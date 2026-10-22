# catalog Specification

## Purpose
管理槟榔商品的分类、列表、详情、SKU 与库存流转，是下单与防超卖的核心数据基础，并为订单预占、支付扣减与售后回补提供库存原语。

## Requirements

### Requirement: P-01 商品分类
系统 SHALL 支持单层商品分类（如「经典系列」），客户端可按分类筛选商品列表。

#### Scenario: 按分类筛选
- **WHEN** 客户端请求 GET /api/products?category=经典系列
- **THEN** 仅返回该分类下商品

### Requirement: P-02 商品列表
系统 SHALL 仅展示上架商品，并以分页形式返回。

#### Scenario: 下架商品不出现在列表
- **WHEN** 某商品被下架后客户端拉取列表
- **THEN** 该商品不在返回结果中

### Requirement: P-04 商品详情
系统 SHALL 提供 GET /api/products/{id} 返回主图、副标题、销量、SKU 列表与可售库存。

#### Scenario: 查看详情
- **WHEN** 买家查看上架商品详情
- **THEN** 返回 SKU 列表及各 SKU 可售库存

### Requirement: P-05 SKU 规格
系统 SHALL 支持 SKU 维度独立价格与库存（如「10 包/袋」），库存挂在 SKU 上。

#### Scenario: SKU 独立库存
- **WHEN** 两个 SKU 库存不同
- **THEN** 各自的可售库存独立计算与扣减

### Requirement: P-06 上下架拦截
系统 SHALL 在下架商品被访问详情或下单时拦截并提示；下架商品客户端列表不可见。

#### Scenario: 下架商品下单被拦
- **WHEN** 对已下架商品发起下单
- **THEN** 系统拒绝并提示商品已下架

### Requirement: S-01 库存结构
系统 SHALL 以「可售 = 总库存 − 锁定库存」表达 SKU 库存，库存挂在 SKU 上。

#### Scenario: 预占导致可售减少
- **WHEN** 订单预占 n 件
- **THEN** 锁定库存 +n、可售库存 −n，总量不变

### Requirement: S-02 原子预占
系统 SHALL 在同一订单多 SKU 场景下原子预占：任一 SKU 库存不足则整单回滚。

#### Scenario: 部分不足整单回滚
- **WHEN** 订单含 A、B 两个 SKU，B 不足
- **THEN** A、B 均不预占，订单创建失败

### Requirement: S-03 支付扣减
系统 SHALL 在支付成功时减少总库存与锁定库存各 n。

#### Scenario: 支付出库
- **WHEN** 待付款订单支付成功
- **THEN** 总库存 −n、锁定库存 −n

### Requirement: S-04 取消释放
系统 SHALL 在主动取消或超时取消时释放锁定库存（锁定 −n）。

#### Scenario: 超时自动取消
- **WHEN** 待付款订单超过 15 分钟未支付
- **THEN** 定时任务将其置为 CANCELED 并释放锁定库存

### Requirement: S-05 防超卖
系统 SHALL 通过数据库条件更新 WHERE total − locked >= n 保证不超卖，受影响行数为 0 即失败（无 Redis 层）。

#### Scenario: 并发下单不超卖
- **WHEN** 可售为 1 时两个用户各下单 1 件
- **THEN** 仅 1 单成功，另 1 单提示库存不足，库存无负数

### Requirement: S-06 库存调整留痕
系统 SHALL 支持管理端库存调整（幂等：delta ≠ 0、扣减后不为负），并要求留痕操作人/时间/变更量/原因。

#### Scenario: 调整被拒
- **WHEN** 调整 delta 为 0 或扣减后总库存为负
- **THEN** 系统拒绝调整

### Requirement: S-07 售后回补
系统 SHALL 在退货退款完成后对 SKU 总库存 +n（仅 RETURN_REFUND 触发）。

#### Scenario: 退货回补
- **WHEN** 退货退款流程完成
- **THEN** 对应 SKU 总库存 +n
