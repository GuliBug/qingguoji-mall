# 前端H5客户端 Specification

## Purpose

前端 H5 客户端能力规格基线：约束 frontend-h5/mall-client 页面行为与登录态封装，作为 UI 迭代与 E2E 用例依据。

## Requirements


### Requirement: 网络与登录态封装
所有后端调用 SHALL 经由 `api.ts` 统一封装（Taro.request + `X-Token` 头），token SHALL 存本地 Storage（key `qgj_token`）；后端地址 SHALL 集中在 `BASE` 常量配置。（对应后端 /api 全部接口）

#### Scenario: 未登录请求需登录接口
- **WHEN** 无 token 时调用需登录接口
- **THEN** 后端返回业务错误，前端以未登录态渲染并引导登录

### Requirement: 首页商品流
首页 SHALL 拉取商品分页列表（销量倒序）展示商品卡片，点击跳转详情页；已登录 SHALL 展示会员等级与积分横幅。

### Requirement: 商品详情与加购
详情页 SHALL 支持 SKU 规格选择并展示可售库存（totalStock − lockedStock）；【加入购物车】SHALL 未登录跳登录、已登录调用加购接口；【立即购买】SHALL 携带所选 SKU 直接进入下单流程。

### Requirement: 购物车管理
购物车 SHALL 展示商品名/规格/单价/数量，数量增减 SHALL 调用更新接口（qty=0 即删除）；失效商品（onSale=false）SHALL 单独分组且不可结算；结算 SHALL 组装 lines 调下单接口并跳转订单页。

#### Scenario: 数量归零删除
- **WHEN** 用户将某商品数量减至 0
- **THEN** 该购物车项被删除并从列表移除

### Requirement: 订单列表与操作
订单页 SHALL 按状态 Tab 筛选；待付款 SHALL 展示支付倒计时（依据 payDeadline）并提供支付/取消；待收货 SHALL 提供确认收货；操作成功后 SHALL 刷新列表。

#### Scenario: 支付成功
- **WHEN** 用户点击去支付且后端返回成功
- **THEN** toast 成功并刷新，订单进入待发货

### Requirement: 售后申请与进度
售后申请 SHALL 依据订单状态约束类型（已发货后仅退货退款）、数量与原因；售后进度 SHALL 展示状态机（待审核→待寄回→待收货→完成），WAIT_RETURN 时 SHALL 提供退货物流单号填写。

### Requirement: 会员中心
会员中心 SHALL 提供：未登录登录卡片（手机号+验证码）；已登录会员卡（等级/成长值进度/积分）、每日签到、扫码积分输入、积分流水列表；后端错误 SHALL 以 toast 展示原始 msg（如"该码已被使用"）。

#### Scenario: 扫码积分失败提示
- **WHEN** 输入已核销的防伪码提交
- **THEN** toast 展示"该码已被使用"，页面数据不变
