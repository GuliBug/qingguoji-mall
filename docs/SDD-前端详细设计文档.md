# 青果纪商城 · 前端详细设计文档（SDD）

| 项 | 内容 |
|---|---|
| 文档版本 | v1.0.0 |
| 对应规格 | `specs/spec.md` v1.1.0（含 change-001） |
| 技术栈 | Taro 4.3.0 · Vue 3 · TypeScript · Vite 4.5（编译 H5） |
| 代码位置 | `frontend-h5/mall-client/` |
| 本地服务 | `npm run dev:h5`（当前以 `npx taro build --type h5 --watch` 运行于 **:10086**） |

---

## 1. 总体架构

```
src/
├── app.ts / app.config.ts / app.scss   # 应用入口、页面路由注册、全局样式（红金品牌主题）
├── api.ts                              # 唯一后端访问层（封装 Taro.request + token）
└── pages/
    ├── index/     首页模块
    ├── product/   商品详情模块
    ├── member/    会员中心模块
    ├── cart/      购物车模块
    └── orders/    订单与售后模块
```

**设计原则**：
- 所有后端调用必须经 `api.ts`，页面不直接发请求；
- 登录态 token 存本地 Storage（key `qgj_token`），请求拦截统一附加 `X-Token` 头；
- 页面间传参用 Taro 路由 query（如 `/pages/product/index?id=1`）。

## 2. 模块设计（中文模块名）

### 2.1 网络与登录态模块（api.ts）

- `request(method, path, data)`：统一封装 `Taro.request`，注入 `X-Token`；
- `getToken/setToken/isLoggedIn`：登录态读写；
- 导出 `api` 对象，方法与后端接口一一对应（login/me/checkin/scan/pointsLogs/products/productDetail/cart/addToCart/updateCart/createOrder/pay/cancel/confirm/orders/applyAfterSale/myAfterSales）。
- 后端地址 `BASE = http://localhost:8080`（上线时改为环境配置）。

### 2.2 首页模块（pages/index）

- 加载 `api.products()` 商品流（销量排序，20/页）；
- 会员横幅：已登录显示等级/积分（取 `api.me()`），未登录引导去会员页登录；
- 商品卡片点击跳详情页。

### 2.3 商品详情模块（pages/product）

- 路由参数 `id` → `api.productDetail(id)`；
- SKU 规格选择（单包/整条/整箱），展示 `totalStock − lockedStock` 可售库存；
- 【加入购物车】→ 未登录跳登录，已登录 `api.addToCart(skuId, qty)`；
- 【立即购买】→ 直接携带 `lines` 走下单流程（同购物车结算入口）。

### 2.4 购物车模块（pages/cart）

- `api.cart()` 拉取 `CartView` 列表（商品名/规格/单价/数量/在售标记）；
- 数量增减调 `api.updateCart(cartId, qty)`，qty 归 0 即删除（与后端约定）；
- 失效商品（`onSale=false`）单独分组不可结算；
- 底部结算栏：合计金额 +【去结算】→ 组装 `lines` 调 `api.createOrder`，成功后跳订单页。

### 2.5 订单与售后模块（pages/orders）

- Tab 按状态筛选（全部/待付款/待发货/待收货/已完成），`api.orders(status)`；
- **待付款**：展示支付倒计时（`payDeadline`），【去支付】`api.pay(orderNo)`、【取消订单】`api.cancel(orderNo)`；
- **待收货**：【确认收货】`api.confirm(orderNo)`；
- **售后申请**：从订单明细发起，类型切换 仅退款/退货退款（依据订单状态约束：已发货后仅退货退款），填数量与原因 → `api.applyAfterSale`；
- **售后进度**：`api.myAfterSales()` 列表，状态机展示 WAIT_AUDIT→WAIT_RETURN→WAIT_RECEIVE→DONE；WAIT_RETURN 时填写退货物流单号。

### 2.6 会员中心模块（pages/member）

- 未登录：手机号+验证码（演示 123456）登录卡片 → `api.login`；
- 已登录：会员卡（等级/成长值进度条/积分）、每日签到按钮（`api.checkin`，已签提示）、扫码积分输入（`api.scan`，成功 +10，失败按后端 msg 提示：码已用/超 20 包上限）、积分流水列表（`api.pointsLogs`）。

## 3. 页面路由表（app.config.ts）

| 路由 | 页面 | tabBar |
|---|---|---|
| pages/index/index | 首页 | ✅ 首页 |
| pages/cart/index | 购物车 | ✅ 购物车 |
| pages/member/index | 会员中心 | ✅ 我的 |
| pages/product/index | 商品详情 | — |
| pages/orders/index | 订单/售后 | — |

## 4. 全局样式（app.scss）

- 品牌主题：红金（主色 `#c0392b` 系），积分/价格金色点缀；
- 通用类：`.card`（白底圆角卡片）、`.btn-primary`（主按钮）、`.price`（价格排版）；
- H5 适配：rpx 单位，Taro 自动换算。

## 5. 状态与异常处理约定

| 场景 | 处理 |
|---|---|
| 接口 code ≠ 200 | `Taro.showToast({title: msg, icon:'none'})`，不中断页面 |
| 未登录访问需登录接口 | 后端返回提示 → 跳会员页登录 |
| 库存不足下单失败 | toast 后端 msg（含商品名+规格），购物车保留 |
| 支付超时订单已被定时任务取消 | toast「订单已超时取消」，刷新列表 |

## 6. 待迭代项

1. 图片资源：当前封面用色块占位，后续接 OSS 图床；
2. 收货地址簿、优惠券（spec C-04/05 的券部分）；
3. 微信支付对接（当前为模拟收银台）；
4. 小程序端复用（Taro 一码多端，仅需处理登录态 wx.login）。
