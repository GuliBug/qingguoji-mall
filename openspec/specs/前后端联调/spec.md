# 前后端联调 Specification

## Purpose

前后端联调契约规格：固化接口对账、错误处理与环境约定，防止契约漂移。

## Requirements

### Requirement: 退货退款全链路端到端验证
E2E SHALL 覆盖退货退款完整链路：申请（WAIT_AUDIT）→ 审核通过（WAIT_RETURN）→ 买家填写退货物流（WAIT_RECEIVE）→ 商家确认收货（DONE），并验证库存回补（可售 +qty）与积分按比例扣回。

#### Scenario: 退货物流单号为空被拒
- **WHEN** 买家在 WAIT_RETURN 状态售后单上提交空退货物流单号
- **THEN** 返回"退货物流单号不能为空"，状态不流转

### Requirement: 双端联调对账
E2E SHALL 验证 H5 五个页面模块（index/product/cart/orders/member）编译可达，并 SHALL 自动比对 H5 `api.ts` 全部接口路径与 E2E 实际调用集合，确认前端契约无漂移。

#### Scenario: api.ts 契约对账
- **WHEN** 运行 E2E 对账用例
- **THEN** api.ts 解析出的全部接口（≥15 个）均被本次 E2E 真实执行，未覆盖集合为空


### Requirement: 接口对账
前端 `api.ts` 的每个方法 SHALL 与后端 Controller 接口一一对应（15 组），字段名、类型、路径 SHALL 保持契约一致；任一端变更接口 SHALL 同步更新对账表（`docs/前后端联调文档.md`）。

#### Scenario: 契约变更
- **WHEN** 后端修改某接口入参或返回结构
- **THEN** 对账表与 api.ts 同一 change 内同步更新，联调冒烟重跑

### Requirement: 统一响应与错误处理
后端 SHALL 统一返回 `{code, msg, data}`（成功 code=200）；前端 SHALL 对 code≠200 以 toast 展示 msg 且不中断页面；两套 token（买家/管理员）SHALL 不可混用。

#### Scenario: 业务错误透传
- **WHEN** 下单时库存不足
- **THEN** 前端 toast 展示后端 msg（含商品名与规格），购物车数据保留

### Requirement: 核心链路时序
下单-支付-售后链路 SHALL 满足：下单预占（可售↓）→ 支付实扣+发积分 → 售后审核通过按比例扣回积分、退货回补库存；15 分钟未支付 SHALL 由定时任务自动取消并释放库存。

#### Scenario: 全链路数据一致
- **WHEN** 下单 5 包 ¥83 → 支付 → 仅退款 1 包 ¥15 → 审核通过
- **THEN** 积分净变化 +68，库存 540→535 且锁定归零，订单状态正确

### Requirement: 联调环境约定
联调 SHALL 使用：后端 :8080、前端 :10086、H2 内存库（重启重置并由种子数据重建）；演示账号 买家任意手机号+123456、管理端 admin/admin123；每次后端重启后 SHALL 重新登录获取新 token。

### Requirement: 联调冒烟验收
每次联调 SHALL 至少执行 7 条冒烟用例（登录/加购下单/支付/库存核对/仅退款/管理端审核/异常分支）并全部通过，结果记录于联调文档。
