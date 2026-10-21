# 任务清单：harden-field-validation

## 1. 代码加固

- [x] 1.1 MallService：login 手机号/验证码、addToCart skuId/qty、updateCart qty、createOrder 收件三字段+明细、scanCode code、applyAfterSale type/reason、ship logisticsNo
- [x] 1.2 AdminController：changeStatus status 枚举、adjustStock delta、adjustPoints delta

## 2. 测试

- [x] 2.1 FieldValidationTest 12 例（F01~F12）编写并通过
- [x] 2.2 E2E 新增 E2E-26~35 字段级用例并通过
- [x] 2.3 全量回归：单测 25/25，E2E 35/35

## 3. 文档与归档

- [x] 3.1 测试用例集 v1.1.0、测试报告 v1.1.0 更新
- [x] 3.2 参数校验模块规格 delta 归档至 openspec/specs/，本 change 移入 archive
