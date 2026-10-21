# 商品库存模块 规格增量

## ADDED Requirements

### Requirement: 商品列表与详情
系统 SHALL 提供商品分页列表（按销量倒序，支持分类筛选）与详情（含 SKU 列表），仅展示在售商品。（P-01/P-02）

#### Scenario: 分类筛选
- **WHEN** 携带 category 参数查询
- **THEN** 仅返回该分类的在售商品

### Requirement: 库存模型
SKU SHALL 维护 `totalStock`（总库存）与 `lockedStock`（锁定库存），可售库存 = totalStock − lockedStock。（S-01）

### Requirement: 库存四原语（防超卖）
系统 SHALL 以数据库条件更新实现：下单预占（lockStock）、支付扣减（deductStock）、取消释放（releaseStock）、退货回补（restock）；预占 SQL SHALL 带 `total_stock - locked_stock >= n` 条件，任一行失败整体回滚。（S-02~S-05 / AC-01）

#### Scenario: 库存不足下单
- **WHEN** 下单数量大于可售库存
- **THEN** lockStock 影响 0 行，返回"库存不足"，事务回滚不产生订单

#### Scenario: 并发下单防超卖
- **WHEN** 可售 5 件时并发提交多笔共 8 件的订单
- **THEN** 仅累计 ≤5 件的下单成功，其余返回库存不足

### Requirement: 下架商品约束
已下架商品 SHALL 不可加购、不可下单。（AC-07）

#### Scenario: 下架商品加购
- **WHEN** 对 status≠1 的商品 SKU 加购
- **THEN** 返回"商品已下架，无法加入购物车"
