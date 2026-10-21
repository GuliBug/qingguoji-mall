# 任务清单：add-backend-specs

## 1. 规格编写

- [x] 1.1 会员积分模块规格 delta（覆盖 U-01~04 / V-01~07 / AC-09~12）
- [x] 1.2 商品库存模块规格 delta（覆盖 P-01~06 / S-01~07 / AC-01）
- [x] 1.3 订单模块规格 delta（覆盖 O-01~08 / AC-01~02）
- [x] 1.4 售后模块规格 delta（覆盖 A-01~09 / AC-05/AC-12）
- [x] 1.5 管理端模块规格 delta（覆盖 M-01~09）

## 2. 实现核对（代码已先行落地，逐条反向核对）

- [x] 2.1 MallService 下单/支付/取消/售后与规格一致
- [x] 2.2 ProductSkuRepo 条件更新 SQL（lockStock/deductStock/releaseStock/restock）与规格一致
- [x] 2.3 等级折扣/积分倍率数值表与规格一致
- [x] 2.4 管理端接口与 token 隔离与规格一致

## 3. 验证

- [x] 3.1 全链路冒烟：下单 ¥83 → 支付 +83 积分 → 退款 ¥15 → 扣回 68 → 库存 540→535
- [x] 3.2 规格 Scenario 与冒烟用例一一可对应

## 4. 归档

- [x] 4.1 deltas 合并至 `openspec/specs/`，本 change 移入 `changes/archive/`
