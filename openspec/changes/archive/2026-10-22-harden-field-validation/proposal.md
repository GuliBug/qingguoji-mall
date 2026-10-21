# 提案：harden-field-validation 接口字段级校验加固

## Why（动机）

第二轮字段级测试发现 9 处校验缺口：登录手机号无格式校验、加购/下单/售后/管理端多个字段接受空值或非法值（如售后 type 非法值会流入错误审核分支）。不符合"规格即验收标准"的要求，需代码加固并沉淀为能力规格。

## What Changes（变更内容）

1. **代码加固**（MallService + AdminController）：手机号格式 `^1\d{10}$`、加购 qty∈[1,999]、下单收件三字段必填、售后 type 白名单与 reason 必填、扫码 code 必填、发货 logisticsNo 必填、管理端 delta 非空≠0、商品 status 仅 0/1；
2. **新增测试**：FieldValidationTest 12 例（字段级）+ E2E 字段级 10 例（E2E-26~35）；
3. **新增能力规格**：`参数校验模块`（本 change delta，归档至 openspec/specs/参数校验模块/）。

## Impact（影响范围）

- 接口行为变更：上述字段的非法请求从"落库脏数据/NPE/错误分支"变为返回明确业务错误 msg；
- 正常链路不受影响（E2E 全量 35 例复跑通过验证）。
