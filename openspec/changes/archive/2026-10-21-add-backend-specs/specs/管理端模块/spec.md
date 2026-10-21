# 管理端模块 规格增量

## ADDED Requirements

### Requirement: 管理端独立登录
管理端 SHALL 使用独立账号体系（admin/admin123）登录，签发 admin 类型 token，与买家 token 隔离不可互访。（M-01）

#### Scenario: 令牌隔离
- **WHEN** 使用买家 token 访问 /admin-api 接口
- **THEN** 返回无权限错误

### Requirement: 数据看板
系统 SHALL 提供看板统计：订单数、销售额、会员数、待审核售后数。（M-02）

### Requirement: 商品与库存管理
管理端 SHALL 支持商品上下架、SKU 库存调整，所有变更 SHALL 留痕（操作人、时间、前后值）。（M-03/M-04 / AC-08）

#### Scenario: 调整库存
- **WHEN** 对 SKU 追加库存 100
- **THEN** totalStock +100，留痕可查

### Requirement: 订单发货与售后审核
管理端 SHALL 支持对 PAID 订单发货（记录物流单号）、售后单审核（通过/拒绝）与退货确认收回。（M-05/M-06）

### Requirement: 会员管理与积分调整
管理端 SHALL 支持会员分页查询、手动积分调整（写 ADMIN_ADJUST 流水）与积分流水查询。（M-07~M-09）

#### Scenario: 手动调整积分
- **WHEN** 给会员加 50 积分并填备注
- **THEN** 会员积分 +50，流水新增 ADMIN_ADJUST 记录
