# 参数校验模块 规格增量

## ADDED Requirements

### Requirement: 身份字段校验
登录接口 SHALL 校验手机号格式（`^1\d{10}$`）与验证码非空，非法输入 SHALL 返回明确错误且不创建会员。（U-01）

#### Scenario: 手机号格式非法
- **WHEN** phone 为 null/空/位数不足/非 1 开头
- **THEN** 返回"手机号格式不正确"，不产生会员记录

### Requirement: 交易数量字段校验
加购与下单数量 SHALL 为 1~999 的整数；购物车更新 qty SHALL 非空（0 表示删除）；库存/积分调整 delta SHALL 非空且不为 0。

#### Scenario: 加购数量越界
- **WHEN** qty 为 0、负数或 null → 拒绝"必须大于 0"；qty=1000 → 拒绝"不能超过 999"
- **THEN** 购物车数据不变

### Requirement: 订单收件字段校验
下单 SHALL 校验收货人、收货电话（`^1\d{10}$`）、收货地址均非空，任一缺失 SHALL 拒绝下单且不预占库存。

#### Scenario: 收件信息缺失
- **WHEN** receiver/address 为空或电话格式非法
- **THEN** 返回对应字段错误，库存锁定数为 0

### Requirement: 售后类型与原因校验
售后 type SHALL 仅允许 REFUND_ONLY / RETURN_REFUND（白名单外拒绝，防止流入错误审核分支）；reason SHALL 必填。（A-02）

#### Scenario: 非法售后类型
- **WHEN** type 为 EXCHANGE 或 null
- **THEN** 返回"售后类型不合法"，不产生售后单

### Requirement: 管理端操作字段校验
商品上下架 status SHALL 仅允许 0/1；发货 logisticsNo SHALL 必填；库存与积分调整 delta SHALL 非空且非 0。

#### Scenario: 非法状态值
- **WHEN** status 为 2/-1/null
- **THEN** 返回"状态值不合法"，商品状态不变
