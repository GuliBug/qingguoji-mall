# Spec Delta

## Purpose

管理买家订单的创建、库存预占、模拟支付、状态机流转、取消、列表、确认收货与发货，是交易闭环的核心。

## ADDED Requirements

### Requirement: O-01 创建订单
系统 SHALL 生成订单号 QGJ + yyyyMMddHHmmss + 6 位序列；收件人三字段必填且手机号格式校验；创建与库存预占同一事务。

#### Scenario: 创建并预占
- **WHEN** 买家提交购物车订单
- **THEN** 生成订单并原子预占库存，预占失败整单回滚

### Requirement: O-02 库存预占事务
系统 SHALL 在订单创建同一事务中预占库存，预占失败整单回滚。

#### Scenario: 预占失败回滚
- **WHEN** 任一 SKU 库存不足
- **THEN** 订单创建失败且库存不变化

### Requirement: O-03 模拟支付
系统 SHALL 支持点击即支付成功；非待付款状态重复支付被拒（幂等）。

#### Scenario: 重复支付被拒
- **WHEN** 已支付订单再次支付
- **THEN** 系统拒绝（幂等）

### Requirement: O-04 订单状态机
系统 SHALL 实现状态机：WAIT_PAY → (支付)PAID → (发货)SHIPPED → (确认)COMPLETED；WAIT_PAY → CANCELED；PAID/SHIPPED/COMPLETED → AFTER_SALE。非法流转抛 ServiceException。

#### Scenario: 非法流转被拦
- **WHEN** 对非 PAID 订单发货
- **THEN** 系统提示「订单状态不允许发货」

### Requirement: O-05 取消订单
系统 SHALL 支持待付款主动取消与超 15 分钟自动取消，二者均释放锁定库存。

#### Scenario: 超时自动取消
- **WHEN** 待付款订单超过 15 分钟
- **THEN** 置为 CANCELED 并释放库存

### Requirement: O-06 订单列表
系统 SHALL 支持按状态筛选订单：ALL/WAIT_PAY/PAID/SHIPPED/COMPLETED/CANCELED/AFTER_SALE。

#### Scenario: 按状态筛选
- **WHEN** 请求 GET /api/orders?status=PAID
- **THEN** 仅返回已支付订单

### Requirement: O-07 确认收货
系统 SHALL 仅允许 SHIPPED 状态确认收货，重复确认被拒。

#### Scenario: 非待收货确认被拒
- **WHEN** 对 PAID 订单确认收货
- **THEN** 系统拒绝

### Requirement: O-08 发货
系统 SHALL 仅允许 PAID 状态发货，且物流单号非空。

#### Scenario: 发货填物流单号
- **WHEN** 商家对 PAID 订单发货并填物流单号
- **THEN** 订单置为 SHIPPED 并记录单号
