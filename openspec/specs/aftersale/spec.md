# aftersale Specification

## Purpose
管理买家发起的售后（仅退款/退货退款）申请、商家审核与执行，是交易闭环的逆向环节，覆盖申请、审核、仅退款与退货退款的完整逆向交易链路。

## Requirements

### Requirement: A-01 申请入口
系统 SHALL 允许 PAID/SHIPPED/COMPLETED 状态订单发起售后申请。

#### Scenario: 不可售后状态
- **WHEN** 对 WAIT_PAY 订单申请售后
- **THEN** 系统提示当前状态不可申请售后

### Requirement: A-02 售后类型
系统 SHALL 支持 REFUND_ONLY（仅退款，限未发货）与 RETURN_REFUND（退货退款）；已发货/已完成订单仅退款被拒。

#### Scenario: 已发货仅退款被拒
- **WHEN** 对已发货订单申请仅退款
- **THEN** 系统拒绝

### Requirement: A-03 申请内容约束
系统 SHALL 校验申请数量 ≤ 可售后数量、原因必填、类型在白名单内。

#### Scenario: 数量超限
- **WHEN** 申请数量超过可售后数量
- **THEN** 系统拒绝

### Requirement: A-04 售后状态机
系统 SHALL 实现：WAIT_AUDIT → (通过+仅退款)DONE / (通过+退货退款)WAIT_RETURN → WAIT_RECEIVE → DONE；WAIT_AUDIT → (拒绝)REJECTED。

#### Scenario: 退货退款四段式
- **WHEN** 审核通过退货退款并走完填单/确认收货
- **THEN** 售后置 DONE 并退款 + 回补库存

### Requirement: A-05 商家审核
系统 SHALL 支持商家审核通过/拒绝；拒绝原因可空时填「不符合售后条件」。

#### Scenario: 拒绝留原因
- **WHEN** 商家拒绝售后且未填原因
- **THEN** 系统自动填充默认拒绝原因

### Requirement: A-06 仅退款执行
系统 SHALL 在仅退款审核通过后即完成，无库存回补。

#### Scenario: 仅退款完成
- **WHEN** 仅退款审核通过
- **THEN** 售后 DONE，退款完成，不回补库存

### Requirement: A-07 退货退款执行
系统 SHALL 在退货退款审核通过后，经买家填退货物流、商家确认收货，执行退款 + 回补库存 + 按比例扣回积分与成长值。

#### Scenario: 确认收货回补
- **WHEN** 商家确认收到退货
- **THEN** 退款完成、库存回补、积分/成长值按比例扣回

### Requirement: A-09 金额约束
系统 SHALL 约束退款额 = 明细实付单价 × 数量，且累计不超过明细实付。

#### Scenario: 退款不超付
- **WHEN** 对明细申请退款
- **THEN** 退款额不超过该明细实付总额
