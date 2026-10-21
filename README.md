# 青果纪 · 槟榔品牌会员商城

> SDD 项目：SPEC 驱动开发。需求见 `specs/spec.md`（v1.1.0），变更记录见 `specs/changes/`。

## 项目结构

```
qingguoji-mall/
├── specs/            # SDD 规格仓库（spec.md + changes/ + CHANGELOG.md）
├── backend/          # Spring Boot 3.5 后端（参考若依：统一响应/全局异常/管理端独立登录）
├── frontend-h5/      # Taro 4 + Vue3 + TS 客户端 H5
├── docs/             # HTML 原型（mall-prototype.html）+ 部署文档
└── README.md
```

## 后端启动（D:\projects\qingguoji-mall\backend）

```bash
# 方式一：开发运行
mvnw.cmd spring-boot:run

# 方式二：打包运行
mvnw.cmd -DskipTests package
java -jar target/mall-backend-0.0.1-SNAPSHOT.jar
```

- 默认使用 **H2 内存库**（免装 MySQL 直接跑），控制台：http://localhost:8080/h2-console
- 生产/联调切 MySQL：`--spring.profiles.active=mysql`（先建库 `qingguoji_mall`，连接配置在 application.yml）
- 端口 8080；种子数据启动自动导入（10 个槟榔商品 + 30 个防伪码）

### 管理端账号

| 账号 | 密码 |
|---|---|
| admin | admin123 |

### Web 管理端入口（若依风格）

后端启动后直接访问：**http://localhost:8080/admin.html**（同源托管免 CORS），包含数据看板、商品管理（上下架）、库存管理（调整）、订单管理（发货）、售后管理（审核/确认收货）、会员管理（积分调整/流水）六大模块。

## 核心接口清单

### 客户端 `/api`
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/member/login | 手机号+验证码登录（验证码固定 123456） |
| GET | /api/member/me | 会员信息（等级/倍率/折扣） |
| POST | /api/member/checkin | 每日签到 +5 积分 |
| POST | /api/member/scan | 扫码积分 +10（每日 20 包上限） |
| GET | /api/member/points/logs | 积分明细 |
| GET | /api/products?category=&page= | 商品列表（仅上架） |
| GET | /api/products/{id} | 商品详情+SKU |
| GET | /api/cart | 购物车 |
| POST | /api/cart | 加购 |
| POST | /api/orders | 下单（预占库存） |
| POST | /api/orders/{no}/pay | 模拟支付（扣库存+发积分） |
| POST | /api/orders/{no}/cancel | 取消订单（释放库存） |
| POST | /api/orders/{no}/confirm | 确认收货 |
| POST | /api/aftersale | 申请售后（仅退款/退货退款） |
| POST | /api/aftersale/{id}/return-logistics | 买家填退货物流 |

### 管理端 `/admin-api`（Header: X-Token）
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /admin-api/login | 管理员登录 |
| GET | /admin-api/dashboard | 数据看板 |
| GET | /admin-api/products | 商品管理列表 |
| POST | /admin-api/products/{id}/status | 上下架 |
| GET | /admin-api/skus | SKU 库存列表 |
| POST | /admin-api/skus/{id}/stock | 库存调整（留痕） |
| GET | /admin-api/orders | 订单列表 |
| POST | /admin-api/orders/{no}/ship | 发货 |
| GET | /admin-api/aftersales | 售后列表 |
| POST | /admin-api/aftersales/{id}/audit | 售后审核 |
| POST | /admin-api/aftersales/{id}/confirm-return | 确认收退货并退款 |
| GET | /admin-api/members | 会员列表 |
| POST | /admin-api/members/{id}/points | 积分人工调整 |

登录态：`X-Token` 请求头（当前为内存版 token，正式版按 spec U-02 换 JWT）。

## 快速验证（AC 抽查）

```bash
# 登录
curl -X POST localhost:8080/api/member/login -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","smsCode":"123456"}'

# 商品列表
curl localhost:8080/api/products

# 下单（skuId 换成商品详情里返回的）→ 支付 → 查积分
```

## 前端启动（frontend-h5/mall-client）

```bash
npm install
npm run dev:h5     # 浏览器打开提示的 localhost 端口
```

## 与 SPEC 的对应关系

- 订单状态机 / 库存预占扣减释放 / 售后状态机 → `MallService`（类注释标注了对应 spec 编号）
- 防超卖：`ProductSkuRepo.lockStock` 条件更新（S-05）+ 单事务原子预占（S-02）
- 订单超时取消：`OrderTimeoutScheduler`（O-05，30s 扫描，正式版换 Redis 过期监听）
- 积分/等级：`Member.pointRate()/discount()`（V-01~V-03）
