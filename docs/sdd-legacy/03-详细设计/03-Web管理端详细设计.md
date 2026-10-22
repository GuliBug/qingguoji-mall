# 03-3 · Web 管理端详细设计

| 项 | 内容 |
|---|---|
| 文档编号 | QGJ-LLD-WEB-03-3 |
| 版本 | v2.0.0 |
| 日期 | 2026-10-22 |
| 工程 | `frontend-web/`：59 个源文件，Vue 3.5 + Element Plus 2.13 + Vite 6 |
| 参考 | RuoYi-Vue3（`yangzongzhuan/RuoYi-Vue3`）目录结构与交互约定 |

---

## 1. 工程结构

```
frontend-web/
├── index.html
├── package.json
├── vite.config.js               # :5173 + /admin-api 代理
├── vitest.config.js             # jsdom 环境，tests/unit
├── eslint.config.js             # ESLint 9 flat config
├── .prettierrc.json
├── .env.development             # VITE_APP_BASE_API=/admin-api, TARGET=localhost:8080
├── .env.production
├── src/
│   ├── main.js                  # createApp + 插件 + 全局组件 + 图标批量注册
│   ├── App.vue
│   ├── permission.js            # 全局路由守卫
│   ├── settings.js              # 标题、侧栏主题、是否显示标签页
│   ├── api/                     # 8 个业务文件 + page.js 分页适配
│   ├── assets/styles/           # index / variables / ruoyi / element-plus / sidebar
│   ├── components/              # Pagination / RightToolbar / DictTag
│   ├── directive/               # v-hasPermi / v-hasRole
│   ├── layout/                  # index.vue + Sidebar / Navbar / Breadcrumb / TagsView / AppMain
│   ├── plugins/                 # cache（session/local）· modal（$modal/$tab）· 全局挂载
│   ├── router/index.js          # constantRoutes + asyncRoutes
│   ├── store/                   # index.js + modules/{user,app,permission,settings}
│   ├── utils/                   # request / auth / validate / format / dict / ruoyi / errorCode
│   └── views/                   # login + dashboard + 5 个业务页 + 404 + redirect
└── tests/
    ├── unit/                    # 4 个 spec，26 个用例
    └── e2e/                     # login / navigation 两个套件，10 个用例
```

---

## 2. 启动与构建

| 命令 | 作用 |
|---|---|
| `npm run dev` | 开发服务器 `:5173`，代理 `/admin-api → http://localhost:8080` |
| `npm run build:prod` | 生产构建到 `dist/` |
| `npm run lint` | ESLint 检查 |
| `npm run format` / `format:check` | Prettier 写盘 / 校验 |
| `npm run test:unit` | Vitest 单测 |
| `npm run test:e2e` | Puppeteer E2E（依赖 dev server 已启动） |

**环境变量**

| 变量 | dev | prod | 说明 |
|---|---|---|---|
| `VITE_APP_BASE_API` | `/admin-api` | `/admin-api` | axios baseURL |
| `VITE_APP_TARGET` | `http://localhost:8080` | — | 仅 dev 代理用 |
| `VITE_APP_TITLE` | 青果纪商城管理系统 | 同 | 页面标题 |

---

## 3. 路由设计

### 3.1 静态路由（`constantRoutes`）

| 路径 | 组件 | 说明 |
|---|---|---|
| `/login` | `views/login.vue` | `hidden` |
| `/404` | `views/error/404.vue` | `hidden` |
| `/redirect/:path(.*)` | `views/redirect/index.vue` | 用于标签页刷新，`hidden` |
| `/` → `/index` | `views/dashboard/index.vue` | 首页看板，`affix` 固定标签 |

### 3.2 动态路由（`asyncRoutes`）

| 路径 | 组件 | 菜单标题 | 图标 |
|---|---|---|---|
| `/product` | `views/product/index.vue` | 商品管理 | `Goods` |
| `/stock` | `views/stock/index.vue` | 库存管理 | `Box` |
| `/order` | `views/order/index.vue` | 订单管理 | `Tickets` |
| `/aftersale` | `views/aftersale/index.vue` | 售后管理 | `RefreshLeft` |
| `/member` | `views/member/index.vue` | 会员管理 | `User` |

**关键约束：动态路由不能同时出现在 `constantRoutes` 里。**
初版把业务路由既写进 `createRouter` 又用 `addRoute` 注入，同一 `path` 被注册两次，导致 SPA 内部跳转后 `<router-view>` 渲染空白（首屏正常、点菜单就白）。现拆为两套：`createRouter` 只收 `constantRoutes`，登录后 `addRoute` 注入 `asyncRoutes`。**这个 bug 已由 E2E 用例「点击侧边栏菜单可跳转路由」锁定。**

### 3.3 路由守卫（`permission.js`）

```
beforeEach(to):
  NProgress.start()
  if 有 token:
      if to.path == '/login' → 重定向 '/'
      if 命中白名单 → 放行
      if store.roles 为空:                      ← 刷新后首次进入
          getInfo()  （实际调 /admin-api/dashboard 验活）
          generateRoutes() → 逐条 addRoute
          return { ...to, replace: true }       ← 重新走一遍匹配
      else 放行
  else:
      if 命中白名单 → 放行
      → /login?redirect=<fullPath>
```

**为什么不调 `/getInfo`**：后端没有该接口。`getInfo()` 复用 `checkToken()`（指向 `/admin-api/dashboard`），能返回 200 即说明令牌有效，顺带把看板数据预热了。

**角色处理**：`roles` 固定为 `['admin']`，`permissions` 为 `['*:*:*']`，因此 `v-hasRole` / `v-hasPermi` 指令当前永远放行。指令已实现，待后端补权限模型后即可生效。

---

## 4. 状态管理（Pinia）

| Store | state | 主要 actions | 说明 |
|---|---|---|---|
| `user` | `token` `name` `avatar` `roles` `permissions` | `login` `getInfo` `logOut` | token 存 Cookie（`js-cookie`），刷新不丢 |
| `permission` | `routes` `addRoutes` `sidebarRouters` | `generateRoutes` | 返回待注入的路由数组 |
| `app` | `sidebar.opened` `device` | `toggleSidebar` | 侧栏折叠，状态存 localStorage |
| `settings` | `title` `sideTheme` `showSettings` `tagsView` `fixedHeader` | — | 界面偏好 |

**Cookie 命名**：`Admin-Token`，与若依一致，便于沿用社区工具函数。

---

## 5. 请求层

`utils/request.js` 是若依原版逻辑，做了三处适配：

| 能力 | 实现 |
|---|---|
| 带 token | 请求拦截器写 `config.headers['X-Token'] = getToken()`；`isToken:false` 可跳过 |
| GET 参数序列化 | `tansParams` 拼到 URL，去掉末尾 `&`（后端不接受数组括号语法） |
| 防重复提交 | POST/PUT 1 秒内相同 url + 相同 body 直接被拒，提示"数据正在处理，请勿重复提交" |
| 401 处理 | 弹 `ElMessageBox` 确认框 → 确认后 `logOut()` + `location.href = '/index'`（不整页刷新会残留旧路由） |
| 错误提示 | `errorCode[code] || res.data.msg`，统一 `ElNotification` 展示 |

**分页适配（`api/page.js`）**

```js
buildPageParams({ pageNum = 1, pageSize = 10, ...rest })   // → { page: pageNum-1, size: pageSize, ...rest }
unwrapPage(res)      // Spring Page{content,totalElements} → { rows, total }
                     // 已是 {rows,total} 则原样返回；空值返回 { rows: [], total: 0 }
unwrapList(list)     // 数组 → { rows: list, total: list.length }
```

`unwrapPage` 双兼容是刻意设计：后端 `/api/**` 与 `/admin-api/**` 分页结构不同，前端不用关心调的是哪一套。

---

## 6. 布局

```
layout/index.vue
├── Sidebar/                    深色侧栏（#304156），可折叠，宽 210/64px
│   ├── Logo.vue                标题 + 折叠过渡
│   ├── index.vue               el-menu 容器，遍历 sidebarRouters
│   └── SidebarItem.vue         递归渲染，支持二级菜单
├── Navbar.vue                  折叠按钮 + 面包屑 + 右侧用户下拉（退出登录）
├── TagsView/                   多标签页，支持关闭/刷新，affix 标签不可关
├── AppMain.vue                 <router-view> + keep-alive + 过渡动画
└── Breadcrumb.vue              根据 matched 生成
```

**主内容区高度**：`calc(100vh - 50px - 34px)`，保证表格在视口内滚动而非整页滚动。

---

## 7. 页面设计

### 7.1 登录页 `views/login.vue`

| 项 | 设计 |
|---|---|
| 布局 | 左侧品牌插画（SVG 背景），右侧表单卡片；窄屏隐藏左侧 |
| 字段 | 用户名（默认填 `admin`）、密码（默认填 `admin123`，演示便利） |
| 校验 | 必填、密码长度 ≥ 5 |
| 提交 | `userStore.login()` → 成功 `router.push(redirect || '/')`；失败 toast |
| 提示 | 卡片底部标注"演示账号 admin / admin123" |

### 7.2 数据看板 `views/dashboard/index.vue`

**6 张指标卡**

| 卡片 | 数据来源 | 配色 |
|---|---|---|
| 今日销售额 | `todaySales`（⚠️ 恒为 0） | 蓝 `#409eff` |
| 待发货订单 | `waitShipOrders` | 橙 `#e6a23c` |
| 待审核售后 | `waitAuditAfterSales` | 红 `#f56c6c` |
| 会员总数 | `memberCount` | 绿 `#67c23a` |
| 低库存 SKU | `lowStockSkuCount` | 红 `#f56c6c` |
| 待办合计 | 派生：待发货 + 待审核 | 灰 `#909399` |

**两个面板**：待办事项列表（点击跳对应页面）、系统信息（后端/前端版本、数据说明）。

**刷新**：`setInterval` 30 秒；组件卸载时 `clearInterval`。

### 7.3 商品管理 `views/product/index.vue`

| 项 | 内容 |
|---|---|
| 搜索 | 商品名称（输入框）、分类（下拉，选项由已加载数据去重生成）、状态（在售/已下架） |
| 工具栏 | 刷新、列显隐 |
| 表格列 | ID、封面（60×60 缩略图）、商品名称、分类、**价格区间**（由 SKU 聚合 `min~max`）、销量、积分规则、状态（`el-tag` 绿/灰）、创建时间（180px）、操作 |
| 操作 | 上架/下架 → `ElMessageBox.confirm` 二次确认 → `POST /admin-api/products/{id}/status` |
| 分页 | `Pagination` 组件，10/20/50 可切 |

**数据来源**：商品列表 + SKU 全量列表两个请求并行，前端按 `productId` 聚合出价格区间与可售库存。

**筛选为本地过滤**：后端 `/admin-api/products` 不支持条件查询，`filteredList` 计算属性在已加载数据上过滤。分页也随之为前端分页（数据量大时需后端补查询参数，见技术债）。

### 7.4 库存管理 `views/stock/index.vue`

| 项 | 内容 |
|---|---|
| 搜索 | 商品（下拉）、规格/编码（模糊）、库存预警（全部 / 仅可售 < 10） |
| 表格列 | SKU ID、所属商品、规格、SKU 编码、单价、总库存、锁定库存、**可售库存**（红字高亮，`total - locked`）、操作 |
| 操作 | 调整库存 → 弹窗输入变动数量（正入库/负出库）+ 原因 → `POST /admin-api/skus/{id}/stock` |
| 校验 | `delta` 必填且不为 0；前端提示后端返回的错误 |

### 7.5 订单管理 `views/order/index.vue`

| 项 | 内容 |
|---|---|
| 搜索 | 订单号（模糊）、订单状态（下拉：待付款/待发货/待收货/已完成/已取消/售后中） |
| 表格列 | 订单号、会员 ID、实付金额、状态（彩色 tag：黄/蓝/青/绿/灰/红）、物流单号、创建时间、操作 |
| 展开行 | 展示订单明细：商品、规格、单价、数量、小计、已售后数量 |
| 操作 | 发货（仅 `PAID` 可见）→ 弹窗必填物流单号 → `POST /admin-api/orders/{orderNo}/ship` |

### 7.6 售后管理 `views/aftersale/index.vue`

| 项 | 内容 |
|---|---|
| 搜索 | 售后单号、售后状态（待审核/待寄回/待收货/已完成/已拒绝）、售后类型 |
| 表格列 | 售后单号、类型（仅退款/退货退款 tag）、订单 ID、会员 ID、数量、退款金额、状态、退货物流、申请时间、操作 |
| 操作 | `WAIT_AUDIT`：通过 / 拒绝（拒绝弹窗必填原因）；`WAIT_RECEIVE`：确认收货并退款（二次确认） |
| 状态说明 | 通过 + 仅退款 → 直接 `DONE`；通过 + 退货退款 → `WAIT_RETURN` |

### 7.7 会员管理 `views/member/index.vue`

| 项 | 内容 |
|---|---|
| 搜索 | 手机号（模糊）、会员等级（注册/铜/银/金/黑金） |
| 表格列 | ID、手机号、昵称、等级（按等级配色 tag）、积分（红色）、成长值、注册时间、操作 |
| 操作 | 调整积分（弹窗：当前积分只读 + 变动积分 + 原因）→ `POST /admin-api/members/{id}/points`；查看流水（抽屉/弹窗，`GET /admin-api/members/{id}/points/logs`） |

---

## 8. 样式方案

| 文件 | 内容 |
|---|---|
| `variables.scss` | 若依配色变量：`$blue:#409EFF`、`$menuBg:#304156`、`$menuText:#bfcbd9`、`$menuActiveText:#409EFF` |
| `index.scss` | 全局重置、通用类（`.app-container` `.mb8` `.fr` `.df`） |
| `element-plus.scss` | Element Plus 组件微调（表格行高、卡片圆角、按钮间距） |
| `ruoyi.scss` | 若依风格工具类（搜索表单、表格工具栏、树形缩进） |
| `sidebar.scss` | 侧栏深色主题、折叠态、子菜单背景 `#1f2d3d` |

**Element Plus 引入方式**：全量引入（`app.use(ElementPlus)`）+ 中文 locale，换取开发期零心智负担；代价是打包体积较大，生产可改按需引入（技术债）。

---

## 9. 与若依原版的差异

| 项 | 若依 | 本项目 | 原因 |
|---|---|---|---|
| 请求头 | `Authorization: Bearer xxx` | `X-Token: <token>` | 后端既有约定，H5 端同样使用 |
| 登录信息接口 | `GET /getInfo` | `GET /admin-api/dashboard` | 后端未实现 getInfo，复用验活 |
| 菜单来源 | `GET /getRouters` 后端下发 | 前端 `asyncRoutes` 静态表 | 后端无菜单表，前端写死 5 个菜单 |
| 权限模型 | RBAC + 细粒度按钮权限 | 单管理员，`permissions=['*:*:*']` | 本期只有一种管理员角色 |
| 分页结构 | `{ code, rows, total }` | `{ code, msg, rows, total }` | 与后端 `AjaxResult` 对齐 |
| 字典 | `useDict` 调后端字典接口 | `utils/dict.js` 前端常量表 | 后端无字典接口，静态枚举足够 |
| 状态码 | `code === 200` | 同，额外兼容 `data.content` 分页 | 需同时兼容两套后端接口 |

---

## 10. 已知限制

| # | 限制 | 影响 | 处理 |
|---|---|---|---|
| 1 | 商品/会员/订单列表的筛选为本地过滤 | 数据量大时分页与筛选结果不准 | 后端补查询参数 |
| 2 | 今日销售额恒 0 | 看板指标不完整 | 待后端补支付流水 |
| 3 | 无商品新建/编辑/删除 | 商品只能靠种子数据 + 上下架 | 列入变更候选 |
| 4 | Element Plus 全量引入 | 首包约 1MB（gzip 后 300KB+） | 可改按需 |
| 5 | 无路由级权限（菜单全可见） | 单角色场景无影响 | 待后端权限模型 |

---

*接口定义见 `01-接口规范.md`；E2E 用例见 `05-测试方案与报告.md`。*
