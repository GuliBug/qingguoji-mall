# 青果纪商城 · Web 管理端

基于 **RuoYi-Vue3 代码风格**的后台管理前端，与后端（Spring Boot）完全分离部署。

## 技术栈

| 分类 | 选型 |
|---|---|
| 框架 | Vue 3.5 |
| UI | Element Plus 2.13 |
| 状态管理 | Pinia 3 |
| 路由 | Vue Router 4（动态路由 + 全局守卫） |
| 请求 | Axios（统一封装 + 拦截器） |
| 构建 | Vite 6 |
| 代码规范 | ESLint 9（flat config）+ Prettier 3 |
| 单元测试 | Vitest 3.2.4 |
| E2E 测试 | Puppeteer 24.10.0 |

## 目录结构（对齐若依 ruoyi-ui）

```
src/
├── api/                  # 接口模块，一个业务一个文件
│   ├── login.js          # 登录 / 校验登录态 / 退出
│   ├── dashboard.js      # 数据看板
│   ├── product.js        # 商品管理
│   ├── sku.js            # SKU 库存
│   ├── order.js          # 订单
│   ├── aftersale.js      # 售后
│   ├── member.js         # 会员
│   └── page.js           # 分页参数适配
├── assets/styles/        # 全局样式（variables / element-plus / sidebar / ruoyi）
├── components/           # 公共组件
│   ├── Pagination/       # 分页（若依同名组件）
│   ├── RightToolbar/     # 表格右上角工具栏
│   └── DictTag/          # 状态字典标签
├── directive/            # 自定义指令 v-hasRole / v-hasPermi
├── layout/               # 布局：Sidebar / Navbar / TagsView / AppMain
├── plugins/              # cache、modal、全局挂载
├── router/index.js       # constantRoutes（静态）+ asyncRoutes（动态）
├── store/modules/        # user / app / permission / settings
├── utils/
│   ├── request.js        # axios 封装（请求/响应拦截、401 重登、防重复提交）
│   ├── auth.js           # token 存取（Cookie: Admin-Token）
│   ├── errorCode.js      # 错误码文案
│   ├── validate.js       # 通用校验
│   ├── ruoyi.js          # 通用工具（parseTime / tansParams / handleTree）
│   ├── format.js         # 金额、时间格式化
│   └── dict.js           # 业务状态字典
├── views/
│   ├── login.vue         # 登录
│   ├── dashboard/        # 数据看板
│   ├── product/          # 商品管理
│   ├── stock/            # 库存管理
│   ├── order/            # 订单管理
│   ├── aftersale/        # 售后管理
│   ├── member/           # 会员管理
│   └── error/404.vue
├── App.vue
├── main.js
├── permission.js         # 全局路由守卫（NProgress + 白名单 + 动态路由）
└── settings.js           # 系统配置（标题 / 侧边栏主题 / tagsView）
```

## 启动

```bash
npm install
npm run dev          # http://localhost:5173
```

开发环境通过 Vite proxy 把 `/admin-api` 转发到 `http://localhost:8080`，因此没有跨域问题。
目标地址在 `.env.development` 的 `VITE_APP_TARGET` 中配置。

默认账号：`admin / admin123`

## 可用命令

| 命令 | 说明 |
|---|---|
| `npm run dev` | 启动开发服务（5173） |
| `npm run build:prod` | 生产构建到 `dist/` |
| `npm run preview` | 预览构建产物（5174） |
| `npm run lint` | ESLint 检查 |
| `npm run lint:fix` | ESLint 自动修复 |
| `npm run format` | Prettier 格式化 |
| `npm run format:check` | Prettier 校验（CI 用） |
| `npm test` | Vitest 单元测试 |
| `npm run test:watch` | Vitest 监听模式 |
| `npm run test:e2e` | Puppeteer E2E（需先启动 dev 服务与后端） |

## 与后端接口的对应关系

| 前端 api 文件 | 后端接口 |
|---|---|
| `api/login.js` → `login` | `POST /admin-api/login` |
| `api/login.js` → `checkToken` | `GET /admin-api/dashboard`（借其校验 token 有效性） |
| `api/dashboard.js` | `GET /admin-api/dashboard` |
| `api/product.js` | `GET /admin-api/products`、`POST /admin-api/products/{id}/status` |
| `api/sku.js` | `GET /admin-api/skus`、`POST /admin-api/skus/{id}/stock` |
| `api/order.js` | `GET /admin-api/orders`、`POST /admin-api/orders/{no}/ship` |
| `api/aftersale.js` | `GET /admin-api/aftersales`、`POST /admin-api/aftersales/{id}/audit`、`.../confirm-return` |
| `api/member.js` | `GET /admin-api/members`、`POST /admin-api/members/{id}/points`、`GET /admin-api/members/{id}/points/logs` |

## 与若依原版的差异（重要）

若依原版对接的是它自己的后端（`/login`、`/getInfo`、`/getRouters`、`Authorization: Bearer`），
本项目后端是自定义的 `mall-backend`，因此做了 4 处适配，均在代码注释中标明：

1. **token 头**：后端用 `X-Token`，不是 `Authorization: Bearer`。位置：`utils/request.js` 请求拦截器。
2. **登录态校验**：后端没有 `/admin-api/getInfo`，`store/modules/user.js` 的 `getInfo()` 改为请求
   `/dashboard` 校验 token 是否有效，角色固定为 `admin`。后端补上接口后改 `api/login.js` 的 url 即可。
3. **菜单**：后端没有 `/getRouters`，菜单用 `router/index.js` 的 `asyncRoutes` 前端静态定义，
   `store/modules/permission.js` 的 `generateRoutes()` 返回它，再由守卫 `addRoute` 注入（与若依流程一致）。
4. **分页结构**：后端返回 Spring Data `Page`（`content`/`totalElements`），
   `utils/request.js` 的 `unwrapPage()` 统一转成若依习惯的 `{ rows, total }`；
   `api/page.js` 的 `toPageParams()` 负责 `pageNum(1 起) → page(0 起)` 的转换。

## 后端字段注意点

- `ProductSku.availableStock()`、`Member.level()/levelName()/pointRate()` 在实体上是普通方法（非 getter），
  不会被 Jackson 序列化。所以：
  - 可售库存由前端计算：`totalStock - lockedStock`；
  - 会员等级用后端返回的 `level`（0~4）映射为「注册会员 / 铜卡 / 银卡 / 金卡 / 黑金卡」。
- 商品实体没有价格字段，价格区间由 `/admin-api/skus` 聚合得出。
- SKU 只返回 `productId`，商品名由 `/admin-api/products` 映射。

## 测试说明

### 单元测试（Vitest）

覆盖纯逻辑层，不依赖后端：

- `tests/unit/format.spec.js`：金额/时间格式化
- `tests/unit/validate.spec.js`：校验工具、parseTime、tansParams、字典映射
- `tests/unit/request.spec.js`：分页适配（Spring Page → rows/total）、pageNum → page 转换

```bash
npm test
```

### E2E 测试（Puppeteer）

真实浏览器跑完整链路，前置条件：**后端已启动（8080）** + **dev 服务已启动（5173）**。

```bash
npm run dev          # 窗口 1
npm run test:e2e     # 窗口 2
```

用例清单（`tests/e2e/`）：

| 文件 | 覆盖点 |
|---|---|
| `login.e2e.js` | 登录页渲染、密码错误提示、登录成功跳转看板、未登录拦截 |
| `navigation.e2e.js` | 六个菜单、菜单点击跳转、商品列表价格聚合、库存可售计算、订单/售后/会员页、退出登录清 token |

每个用例使用独立的 browser context（隔离 cookie），互不污染登录态。
自定义目标地址：`E2E_BASE_URL=http://localhost:5173 npm run test:e2e`
