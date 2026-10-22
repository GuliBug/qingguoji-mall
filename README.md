# 青果纪 · 槟榔品牌会员商城

> SPEC 驱动开发（OpenSpec · spec-driven）。需求/设计/契约/任务存于 `openspec/`（主规格库 + changes 历史），旧静态 SDD 已归档于 `docs/sdd-legacy/`。

## 项目结构

```
qingguoji-mall/
├── openspec/         # SPEC 驱动开发：config.yaml + specs/（主规格库）+ changes/（active + archive）
├── backend/          # Spring Boot 3.5 后端（若依分层：common / framework / mall）
├── frontend-web/     # Web 管理端（Vue3 + Element Plus + Vite，若依 ruoyi-ui 代码风格）
├── frontend-h5/      # Taro 4 + Vue3 + TS 客户端 H5
├── deploy/           # 部署产物：Dockerfile / docker-compose / nginx / systemd / 构建脚本
├── docs/             # HTML 原型 + 部署文档 + sdd-legacy/（旧静态 SDD 归档）
├── AGENTS.md         # OpenSpec 协作约定（工作流/契约权威/质量门禁/提交纪律）
└── README.md
```

## 规格管理（OpenSpec）

本仓库用 OpenSpec 管理 SDD 流程与代码，详见 `AGENTS.md`。核心约定：

- **主规格库**（`openspec/specs/`，按能力组织，共 9 个能力 / 71 条需求）：
  `membership` · `catalog` · `cart` · `order` · `aftersale` · `web-admin` · `h5-client` · `platform` · `api-contract`
- **契约权威**：`api-contract` 能力 + `docs/sdd-legacy/03-详细设计/01-接口规范.md` 是前后端唯一契约来源。
- **增量流程**：新需求/缺陷/重构 → `openspec new change` → 写 proposal/specs delta/design/tasks → 实现 → 质量门禁 → `openspec archive`。
- **质量门禁**（每个 change 收尾任务必须含）：后端 `mvn spotless:check`；前端 `npm run lint` / `format:check` / `test` / `test:e2e` 全绿。
- **禁止 AI 自动 commit / push**，由人工确认后提交。

## 后端代码结构（若依风格）

```
backend/src/main/java/com/qingguoji/mall/
├── QingguojiMallApplication.java     启动类
├── common/                           公共层（无业务）
│   ├── constant/                     Constants / HttpStatus
│   ├── core/controller/              BaseController（success / getDataTable / getMemberId）
│   ├── core/domain/                  AjaxResult（{code, msg, data}）
│   ├── core/page/                    TableDataInfo（{total, rows}）
│   ├── exception/                    ServiceException / GlobalExceptionHandler
│   └── utils/                        StringUtils
├── framework/                        框架层
│   ├── config/                       WebMvcConfig（CORS + 拦截器）/ DemoDataInitializer
│   ├── security/                     LoginInterceptor / LoginHelper / @RequireLogin
│   ├── task/                         OrderTimeoutTask（订单超时取消）
│   └── web/service/                  TokenService（令牌签发与校验）
└── mall/                             业务模块
    ├── controller/                   客户端接口（/api）
    ├── controller/admin/             管理端接口（/admin-api，按模块拆分 7 个 Controller）
    ├── domain/                       实体（JPA）
    ├── domain/dto/                   请求体（XxxBody）
    ├── domain/vo/                    展示对象（CartItemVo）
    ├── repository/                   数据访问（Spring Data JPA，对应若依 Mapper）
    └── service/ + service/impl/      业务接口与实现（6 个服务）
```

分层约定：

| 层 | 职责 | 禁止 |
|---|---|---|
| Controller | 路由、参数绑定、响应包装 | 不写业务判断、不直接注入 Repository |
| Service | 业务规则、事务边界、状态机 | 不碰 HttpServletRequest |
| Repository | 单表查询与原子更新（库存原语） | 不写跨表业务编排 |

登录态：`@RequireLogin(MEMBER/ADMIN)` 标注在 Controller 类或方法上，`LoginInterceptor` 解析 `X-Token` 写入 `LoginHelper`，业务层用 `getMemberId()` 取值，**不再手工校验 token**。

## 后端启动（D:\projects\qingguoji-mall\backend）

```bash
# 方式一：开发运行（需 JDK 21）
mvn spring-boot:run

# 方式二：打包运行
mvn -DskipTests package
java -jar target/mall-backend-0.0.1-SNAPSHOT.jar
```

- 默认使用 **H2 内存库**（免装 MySQL 直接跑），控制台：http://localhost:8080/h2-console
- 生产/联调切 MySQL：`--spring.profiles.active=mysql`（先建库 `qingguoji_mall`）
- 端口 `SERVER_PORT`（默认 8080）；连接参数可用 `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD` 覆盖
- 种子数据启动自动导入（10 个槟榔商品 + 30 个防伪码），生产用 `DEMO_DATA=false` 关闭
- 健康检查：`GET /actuator/health`

### 代码格式化（Spotless）

后端格式由 Maven 插件统一管理，风格为 google-java-format **AOSP**（4 空格缩进，贴近若依）：

```bash
mvn spotless:apply     # 格式化全部 Java 代码
mvn spotless:check     # 只校验（mvn verify 时自动执行，不通过则构建失败）
mvn verify -Dspotless.check.skip=true   # 临时跳过格式校验
```

IDEA 建议装 `google-java-format` 插件并选 AOSP 方案，与命令行保持一致。

### 管理端账号

| 账号 | 密码 |
|---|---|
| admin | admin123 |

### Web 管理端入口（若依 ruoyi-ui 风格）

管理端已拆为独立前端工程 `frontend-web/`，与后端完全分离：

```bash
cd frontend-web
npm install
npm run dev        # http://localhost:5173  （admin / admin123）
```

包含数据看板、商品管理（上下架）、库存管理（调整）、订单管理（发货）、售后管理（审核/确认收货）、会员管理（积分调整/流水）六大模块。
详细说明见 `frontend-web/README.md`（含 ESLint/Prettier 规范与 Vitest + Puppeteer 测试）。

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

- 订单状态机 / 库存预占扣减释放 / 售后状态机 → `mall/service/impl/`（类注释标注了对应 spec 编号）
- 防超卖：`ProductSkuRepository.lockStock` 条件更新（S-05）+ 单事务原子预占（S-02）
- 订单超时取消：`framework/task/OrderTimeoutTask`（O-05，30s 扫描，正式版换 Redis 过期监听）
- 积分/等级：`Member.pointRate()/discount()`（V-01~V-03）

## 部署到服务器

完整步骤见 **`docs/DEPLOY.md`**，两条路可选：

```bash
# A. 传统部署：本地打包 -> 上传 -> systemd + Nginx + MySQL
deploy\build.cmd                 # 产物在 dist/：app.jar + web/ + h5/
scp -r dist/* user@server:/opt/qingguoji/

# B. Docker 一键（MySQL + 后端 + Nginx）
cd deploy && cp .env.example .env && docker compose up -d --build
```

端口规划：`80` 管理端、`8081` H5、`8080` 后端（仅本机监听）。
