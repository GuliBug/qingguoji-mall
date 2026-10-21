# 提案：add-test-suite 建立自动化测试体系

## Why（动机）

SDD 要求"测试用例从规格 Scenario 派生"。需建立自动化测试并跑通整个需求，验证 openspec 7 个能力规格与实现一致；跑不通则修规格/修代码。

## What Changes（变更内容）

1. 新增后端集成测试 `backend/src/test/.../MallFlowIntegrationTest.java`（12 例，逐条对应 Scenario）；
2. 新增 E2E 端到端脚本 `tests/e2e/e2e-mall.mjs`（25 例，真实 HTTP 全需求链路）；
3. 修正规格文档数值错误：`openspec/specs/会员积分模块/spec.md` 与 `docs/SDD-后端详细设计文档.md` 的等级折扣/倍率表对齐原始需求 V-02 与 `Member.java`；
4. 新增《测试用例集》《测试报告》文档。

## Impact（影响范围）

- 无业务代码改动（首轮 8 个失败均为测试脚本/文档问题，修复后 38/38 通过）；
- 测试纳入后续回归：`mvnw test` + `node tests/e2e/e2e-mall.mjs`。
