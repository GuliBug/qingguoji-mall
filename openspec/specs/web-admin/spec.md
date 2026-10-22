# web-admin Specification

## Purpose
面向商家的 PC 管理后台（Vue3 + Element Plus，若依 ruoyi-ui 风格），处理看板、商品、库存、订单、售后、会员作业与管理员登录。

## Requirements

### Requirement: M-06 管理员登录
系统 SHALL 支持商家以账号密码独立登录，错误时提示「用户名或密码错误」，颁发 ADM- 前缀令牌。

#### Scenario: 错误密码
- **WHEN** 管理员密码错误
- **THEN** 提示「用户名或密码错误」

### Requirement: W-01 登录与失效回跳
Web SHALL 提供账号密码登录页；登录态失效自动回跳登录页并提示。

#### Scenario: 令牌失效
- **WHEN** 请求返回 401
- **THEN** 前端自动登出并跳转登录页

### Requirement: W-02 布局框架
Web SHALL 提供若依风格布局：Logo、菜单、面包屑、标签页、内容区，主色 #409EFF。

#### Scenario: 框架渲染
- **WHEN** 管理员登录进入系统
- **THEN** 展示侧边栏菜单与标签页布局

### Requirement: W-04 破坏性操作二次确认
Web SHALL 对写操作给出反馈 toast，对破坏性操作（如下架、发货）进行二次确认。

#### Scenario: 下架二次确认
- **WHEN** 管理员点击下架商品
- **THEN** 弹出二次确认，确认后生效且 H5 立即可见

### Requirement: M-01 数据看板
系统 SHALL 提供看板：待发货订单数、待审核售后数、会员总数、低库存 SKU 数（可售 < 10）。

#### Scenario: 看板指标
- **WHEN** 管理员查看看板
- **THEN** 返回上述指标（注：今日销售额当前恒为 0，待接支付流水表）

### Requirement: M-02 商品管理
系统 SHALL 提供商品列表与上下架（状态仅允许 0/1）。

#### Scenario: 上下架
- **WHEN** 管理员上下架商品
- **THEN** H5 侧对应商品即时可见性变化

### Requirement: M-05 库存管理
系统 SHALL 提供 SKU 库存列表（总/锁定/可售）与库存调整入口。

#### Scenario: 库存调整
- **WHEN** 管理员调整某 SKU 库存 delta
- **THEN** delta 为 0 或空被拒，扣减后不为负

### Requirement: M-03 订单管理
系统 SHALL 提供订单列表（状态筛选）与发货功能（仅 PAID、物流单号必填）。

#### Scenario: 发货
- **WHEN** 管理员对 PAID 订单发货并填单号
- **THEN** 订单状态变为 SHIPPED

### Requirement: M-04 售后管理
系统 SHALL 提供售后列表（状态筛选）、审核与确认退货收货。

#### Scenario: 审核通过退货退款
- **WHEN** 管理员审核通过并后续确认退货收货
- **THEN** 退款完成、库存回补

### Requirement: M-07 会员管理
系统 SHALL 提供会员列表、人工积分调整（0 或空被拒）与积分流水查看。

#### Scenario: 积分调整
- **WHEN** 管理员调整会员积分
- **THEN** 调整值为 0 或空被拒
