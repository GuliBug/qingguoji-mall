# Spec Delta

## Purpose

维护买家的服务端购物车，支持加购、查看、改数量与结算金额计算，是下单的前置环节。

## ADDED Requirements

### Requirement: C-01 加购
系统 SHALL 支持将 SKU 加入购物车：同 SKU 合并数量；单次数量 1~999；下架商品拒绝加购。

#### Scenario: 同 SKU 合并
- **WHEN** 对同 SKU 连续加购 2 次各 1 件
- **THEN** 购物车该 SKU 数量为 2

#### Scenario: 下架商品加购被拒
- **WHEN** 对下架商品加购
- **THEN** 系统拒绝

### Requirement: C-02 查看购物车
系统 SHALL 返回购物车项（商品名、规格、单价、数量、小计），仅含当前会员自己的项。

#### Scenario: 查看本人购物车
- **WHEN** 已登录会员查看购物车
- **THEN** 返回本人购物车项与各项小计

### Requirement: C-03 修改数量
系统 SHALL 支持修改购物车项数量（≤0 视为删除），且仅能操作自己的项。

#### Scenario: 数量归零删除
- **WHEN** 将某项数量改为 0
- **THEN** 该项从购物车移除

### Requirement: C-04 结算金额
系统 SHALL 计算应付额：先按会员等级折扣，折后额 ≥ 99 免运费否则 +8；应付 = 商品额 − 折扣 + 运费（运费门槛比较折后额）。

#### Scenario: 满 99 免运费
- **WHEN** 折后额 ≥ 99
- **THEN** 运费为 0

#### Scenario: 不满 99 收运费
- **WHEN** 折后额 < 99
- **THEN** 运费为 8

### Requirement: C-05 服务端存储
系统 SHALL 将购物车存储在服务端（cart_item 表按 memberId），支持多端同步。

#### Scenario: 多端同步
- **WHEN** H5 加购后从另一设备查看
- **THEN** 购物车项一致
