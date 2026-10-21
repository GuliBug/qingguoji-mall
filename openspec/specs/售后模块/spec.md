# 售后模块 Specification

## Purpose

后端能力规格基线：与 backend/ 已实现代码对齐，验收标准沿用 spec.md AC-01~14，作为后续 MODIFIED 变更与测试派生的依据。

## Requirements


### Requirement: 售后申请约束
申请售后 SHALL 校验：订单状态 ∈ {PAID, SHIPPED, COMPLETED}；已发货/已完成订单不支持仅退款；售后数量 ≤ 明细 `qty − afterSaleQty`；退款金额 = 明细实付单价（均摊）× 退货数量。（A-02/A-03 / AC-05）

#### Scenario: 已发货订单申请仅退款
- **WHEN** SHIPPED 订单申请 REFUND_ONLY
- **THEN** 返回"已发货/已完成订单仅支持退货退款"

#### Scenario: 超量售后
- **WHEN** 明细 5 件已售后 4 件，再申请 2 件
- **THEN** 返回"售后数量不合法，最多可退 1 件"

### Requirement: 售后审核流
售后单 SHALL 遵循状态机：WAIT_AUDIT →（通过）仅退款直接 DONE / 退货退款进 WAIT_RETURN →（买家填退货物流）WAIT_RECEIVE →（商家确认收回）DONE；拒绝进 REJECTED 且可重新申请。（A-04~A-08）

#### Scenario: 退货退款完整链路
- **WHEN** 审核通过 → 买家填退货物流 → 商家确认收回
- **THEN** 状态依次 WAIT_RETURN → WAIT_RECEIVE → DONE

### Requirement: 退款完成动作
退款完成 SHALL：退货退款类型回补库存（restock）；按退款比例扣回积分（REFUND_BACK 负流水）与成长值；订单若有未售后明细 SHALL 恢复原状态（PAID/SHIPPED）。（A-06~A-09 / S-07 / AC-12）

#### Scenario: 仅退款完成
- **WHEN** 管理端审核通过仅退款单（1 包 ¥15，订单实付 83 发放 83 积分）
- **THEN** 售后 DONE，积分扣回 15，不回补库存
