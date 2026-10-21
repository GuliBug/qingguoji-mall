# Web管理端模块 Specification

## Purpose
Web 管理端能力规格：若依风格管理页面（admin.html），由后端 8080 同源托管，作为 `/admin-api` 14 个接口的 UI 调用方，支撑考评演示的管理端。

## Requirements

### Requirement: 管理端页面托管
系统 SHALL 在 `GET /admin.html` 提供管理端页面（后端 static 同源托管，免 CORS 配置），页面 SHALL 包含数据看板、商品管理、库存管理、订单管理、售后管理、会员管理六大模块。

#### Scenario: 页面可达且模块齐全
- **WHEN** 访问 `/admin.html`
- **THEN** 返回 200 且页面含商品管理/售后管理/会员管理模块与 admin-api 调用代码

### Requirement: 管理端业务操作
管理端 SHALL 支持独立登录、看板统计、商品上下架（二次确认）、SKU 库存调整（弹窗输入 delta）、订单发货（必填物流单号）、售后审核通过与拒绝（拒绝必填原因）、退货退款确认收货、会员积分调整与流水查询。

#### Scenario: 售后退货退款确认收货
- **WHEN** 管理员对 WAIT_RECEIVE 状态售后单点击"确认收货并退款"
- **THEN** 售后单状态流转为 DONE，退款完成并回补库存

#### Scenario: 非法操作拦截
- **WHEN** 对 WAIT_RETURN 状态售后单执行确认收货，或发货/调整输入为空或 0
- **THEN** 后端拒绝并在页面 toast 提示具体原因
