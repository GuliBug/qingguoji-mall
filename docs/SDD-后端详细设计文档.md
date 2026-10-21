# 青果纪商城 · 后端详细设计文档（SDD）

| 项 | 内容 |
|---|---|
| 文档版本 | v1.0.0 |
| 对应规格 | `specs/spec.md` v1.1.0（含 change-001） |
| 技术栈 | Java 21 · Spring Boot 3.5.6 · Spring Data JPA · H2（内存，MODE=MySQL）· Lombok |
| 代码位置 | `backend/`（包根：`com.qingguoji.mall`） |
| 服务端口 | 8080（H2 控制台：`/h2-console`，JDBC `jdbc:h2:mem:mall`，用户 `sa`） |

---

## 1. 总体架构

```
┌────────────── 客户端 H5 (Taro+Vue3, :10086) ──────────────┐
│                    X-Token 请求头鉴权                       │
└──────────────┬──────────────────┬─────────────────────────┘
               ▼ /api/**          ▼ /admin-api/**（管理端独立登录）
┌──────────────────────── Spring Boot 3.5 单体 (:8080) ───────────────────────┐
│ Controller 层   MemberCtrl · ProductCtrl · CartCtrl · OrderCtrl            │
│                 AfterSaleCtrl · AdminCtrl                                  │
│ Service 层      MallService（全部业务规则，@Transactional）                 │
│                 OrderTimeoutScheduler（每分钟超时取消）                     │
│ Repository 层   9 个 JPA Repository（含防超卖条件更新 SQL）                 │
│ Domain 层       Member/Product/ProductSku/CartItem/MallOrder/OrderItem     │
│                 /AfterSale/PointsLog/ScanCode                              │
│ 通用层          R(统一响应) · PageResult · BizException · GlobalExceptionHandler │
│                 TokenStore(内存令牌) · CorsConfig                          │
└──────────────────────────────┬──────────────────────────────────────────────┘
                               ▼
                    H2 内存库（生产 profile 预留 MySQL）
```

**分层职责**：Controller 只做参数接收与鉴权头提取；业务规则全部收敛在 `MallService`；所有写操作 `@Transactional` 保证原子性。

## 2. 模块设计（中文模块名）

### 2.1 通用支撑模块（common / config）

| 类 | 职责 |
|---|---|
| `R<T>` | 统一响应体 `{code, msg, data}`，`R.ok(data)` / `R.fail(code,msg)`；业务成功码 200 |
| `PageResult` | 分页包装（totalElements / content） |
| `BizException` | 业务异常，携带用户可读 msg |
| `GlobalExceptionHandler` | `@RestControllerAdvice`：BizException→其 msg（code=500）；参数校验异常→400；兜底→500 |
| `TokenStore` | 内存令牌表：`Map<token,Principal{type,id}>`；买家/管理员两套前缀 |
| `CorsConfig` | 放行 H5 开发端口跨域 |

### 2.2 会员模块（对应 spec 3.7 / U-01~04、V-01~07）

- **实体 `Member`**：phone、nickname、growth（成长值）、points、createdAt。
- **等级规则**（`Member.level()` / `discount()` / `pointRate()`）：

| 等级 | 成长值门槛 | 折扣 | 积分倍率 |
|---|---|---|---|
| 注册会员 | 0 | 无 | 1.0 |
| 铜卡 | 1000 | 无 | 1.2 |
| 银卡 | 5000 | 95 折 | 1.5 |
| 金卡 | 20000 | 9 折 | 2.0 |
| 黑金卡 | 50000 | 85 折 | 2.5 |

- **登录**：手机号+验证码（演示固定 `123456`），新手机号自动注册（默认昵称"会员+尾号4位"）。签发 token。
- **签到**（V-05）：每日一次 +5 积分，用当天是否已有 `CHECKIN` 流水判重。
- **扫码积分**（V-04 / AC-10）：防伪码唯一且一次性（`ScanCode.used`）；每人每日上限 20 包（按当日 `SCAN` 流水计数）；成功 +10 积分并核销该码。
- **积分流水** `PointsLog`：type ∈ EARN(消费) / SCAN(扫码) / CHECKIN(签到) / REFUND_BACK(退款扣回) / ADMIN_ADJUST(管理端调整)，全量留痕可查。

### 2.3 商品与库存模块（P-01~06、S-01~07）

- **实体**：`Product`（name/category/cover/price/description/sales/status）+ `ProductSku`（spec/price/totalStock/lockedStock）。
- **库存公式**：`可售 = totalStock − lockedStock`。
- **防超卖三原语**（`ProductSkuRepo` 条件更新 SQL，AC-01）：
  - `lockStock`：`UPDATE ... SET locked_stock = locked_stock + :n WHERE id=:id AND total_stock - locked_stock >= :n`，返回受影响行数=0 即库存不足；
  - `deductStock`：支付时锁定转实扣 `total_stock = total_stock - n, locked_stock = locked_stock - n`；
  - `releaseStock`：取消释放 `locked_stock = locked_stock - n`；
  - `restock`：退货回补 `total_stock = total_stock + n`。
- 状态机：下单预占 → 支付扣减 / 超时或手动取消释放 → 退货回补。

### 2.4 购物车模块（C-01~05）

- `CartItem`（memberId+skuId 唯一合并数量）；查询返回 `CartView` DTO（联表组装商品名/封面/规格/在售标记）。
- 下架商品不可加购（AC-07）；`qty<=0` 即删除。

### 2.5 订单模块（O-01~08）

- **单号规则**：`QGJ + yyyyMMddHHmmss + 6位序列`。
- **下单事务**（S-02 原子性）：逐行校验在售/数量 → `lockStock` 预占（任一行失败整体回滚）→ 计算会员折扣与运费（满 99 包邮否则 +8）→ 生成 `WAIT_PAY` 订单 + 明细，`payDeadline = now + 15min`。
- **支付**（S-03）：状态幂等校验（非 WAIT_PAY 拒绝）→ 逐行 `deductStock` → 状态→PAID → 发放积分（实付×倍率）与成长值（=实付）。
- **取消**（O-05）：手动取消与定时任务复用；释放预占库存；幂等（非 WAIT_PAY 直接返回）。
- **超时取消**（AC-02）：`OrderTimeoutScheduler` 每分钟扫描 `payDeadline < now` 的 WAIT_PAY 订单批量取消。
- **发货/收货**：PAID→SHIPPED（记物流单号）→COMPLETED。
- **订单状态机**：`WAIT_PAY → PAID → SHIPPED → COMPLETED`，旁路：`CANCELED`、`AFTER_SALE`（有明细进入售后时置入，全部售后结束按物流单号恢复 PAID/SHIPPED）。

### 2.6 售后退单模块（A-01~09）

- **售后单号**：`AH + yyyyMMddHHmmss + 4位序列`；类型 `REFUND_ONLY`（仅退款）/ `RETURN_REFUND`（退货退款）。
- **申请约束**（AC-05/AC-12）：订单须 PAID/SHIPPED/COMPLETED；已发货后不支持仅退款；数量 ≤ `qty − afterSaleQty`（按明细累加防超退）；退款金额 = 明细单价（实付均摊）× 退货数量。
- **审核流**：`WAIT_AUDIT →(通过) 仅退款: DONE / 退货退款: WAIT_RETURN →(买家填退货物流) WAIT_RECEIVE →(商家确认收货) DONE`；拒绝→REJECTED（可重新申请）。
- **退款完成动作**：退货退款回补库存（S-07）；按 `退款金额/订单实付` 比例扣回积分（`REFUND_BACK` 负流水）与成长值（不为负）。

### 2.7 管理端模块（M-01~09）

- 独立登录 `admin/admin123`，签发 admin 类型 token，与买家令牌体系隔离。
- 能力：数据看板（订单/销售额/会员/待审核售后统计）、商品上下架、SKU 库存调整（留痕）、订单发货、售后审核/确认收回、会员列表、积分调整（`ADMIN_ADJUST` 流水）、会员积分流水查询。

## 3. 接口清单

### 3.1 客户端 `/api`（鉴权头 `X-Token`）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/member/login` | 登录/注册 `{phone, smsCode}` |
| GET | `/api/member/me` | 会员信息+等级+可扫码提示 |
| POST | `/api/member/checkin` | 每日签到 |
| POST | `/api/member/scan` | 扫码积分 `{code}` |
| GET | `/api/member/points/logs` | 积分流水（分页） |
| GET | `/api/products?page&size&category` | 商品列表（销量排序） |
| GET | `/api/products/{id}` | 商品详情（含 SKU 与可售库存） |
| GET/POST | `/api/cart` | 购物车查询 / 加购 `{skuId, qty}` |
| PUT | `/api/cart/{cartId}` | 改数量 `{qty}`（0 删除） |
| POST | `/api/orders` | 下单 `{lines:[{skuId,qty}], receiver, receiverPhone, address}` |
| POST | `/api/orders/{orderNo}/pay` · `/cancel` · `/confirm` | 支付 / 取消 / 确认收货 |
| GET | `/api/orders?status&size` | 我的订单（分页） |
| POST | `/api/aftersale` | 申请售后 `{orderItemId, type, qty, reason, description}` |
| GET | `/api/aftersale/my` | 我的售后单 |
| POST | `/api/aftersale/{id}/return-logistics` | 填写退货物流 `{logisticsNo}` |

### 3.2 管理端 `/admin-api`（admin token）

`POST /login` · `GET /dashboard` · `GET /products` + `POST /products/{id}/status` · `GET /skus` + `POST /skus/{id}/stock` · `GET /orders` + `POST /orders/{orderNo}/ship` · `GET /aftersales` + `POST /aftersales/{id}/audit` + `POST /aftersales/{id}/confirm-return` · `GET /members` + `POST /members/{id}/points` + `GET /members/{id}/points/logs`

## 4. 数据模型（9 表）

`member` · `product` · `product_sku` · `cart_item` · `mall_order` · `order_item` · `after_sale` · `points_log` · `scan_code`（字段见各实体类 JPA 注解；JPA ddl-auto=update 自动建表）。

## 5. 种子数据

`SeedData`（CommandLineRunner）：10 款槟榔商品（各 2-3 个 SKU：单包/整条/整箱）、演示会员 13800001111、管理端账号、一批未使用防伪码。

## 6. 质量与验证

- 全链路冒烟已通过：下单 ¥83（75+8 运费）→ 支付 +83 积分 → 仅退款 1 包 ¥15 → 审核通过 → 积分扣回 68 → 库存 540→535 且锁定归零。
- 后续：JUnit 单测覆盖 `lockStock` 并发与售后金额计算（对应 spec AC-01~14）。
