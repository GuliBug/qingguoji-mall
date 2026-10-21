# 订单模块 Specification

## Purpose

后端能力规格基线：与 backend/ 已实现代码对齐，验收标准沿用 spec.md AC-01~14，作为后续 MODIFIED 变更与测试派生的依据。

## Requirements


### Requirement: 创建订单
系统 SHALL 在同一事务内完成：逐行校验在售与数量 → 预占库存 → 计算会员折扣与运费（满 99 包邮，否则 +8）→ 生成 WAIT_PAY 订单（单号 QGJ+时间戳+序列）与明细，支付截止 = 15 分钟。（O-01/O-02）

#### Scenario: 正常下单
- **WHEN** 注册会员下单 5 包 × ¥15
- **THEN** 实付 = 75 + 8 运费 = 83，SKU 锁定库存 +5，订单 WAIT_PAY

### Requirement: 模拟支付
支付 SHALL 幂等（非 WAIT_PAY 拒绝）：锁定库存转实扣、状态→PAID、按实付×等级倍率发放积分、成长值 +实付金额。（O-03 / S-03 / V-03）

#### Scenario: 重复支付
- **WHEN** 对已支付订单再次调支付接口
- **THEN** 返回"订单状态不允许支付（幂等保护）"，数据不变

### Requirement: 取消与超时取消
取消订单（手动/定时任务）SHALL 释放预占库存并置 CANCELED，操作幂等；定时任务 SHALL 每分钟扫描 payDeadline 过期的 WAIT_PAY 订单批量取消。（O-05 / AC-02）

#### Scenario: 超时自动取消
- **WHEN** 订单超过 15 分钟未支付且定时任务触发
- **THEN** 订单 CANCELED，锁定库存归零，可售恢复

### Requirement: 发货与收货状态机
订单状态机 SHALL 为 WAIT_PAY → PAID → SHIPPED（记物流单号）→ COMPLETED；仅 PAID 可发货，仅 SHIPPED 可确认收货。（O-06~O-08）

#### Scenario: 状态流转
- **WHEN** 管理端对 PAID 订单发货后买家确认收货
- **THEN** 状态依次变为 SHIPPED、COMPLETED
