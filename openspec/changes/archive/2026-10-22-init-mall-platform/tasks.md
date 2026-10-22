# Tasks

## 1. 建立 OpenSpec 工程

- [x] 1.1 在仓库根执行 `openspec init --tools codebuddy --language zh`，生成 openspec/ 与 .codebuddy/
- [x] 1.2 编写 openspec/config.yaml：项目 context、rules（proposal/design/specs/tasks）、operations（apply/archive 质量门禁与契约同步约定）

## 2. 迁移现有 SDD 为基线规格

- [x] 2.1 编写 proposal.md，列出 9 个新能力（membership/catalog/cart/order/aftersale/web-admin/h5-client/platform/api-contract）
- [x] 2.2 逐个编写各能力的 specs delta（ADDED Requirements + Scenario），保留原 SRS 编号作为追溯锚点
- [x] 2.3 编写 design.md，沉淀架构决策并列出已知实现差距（gap）
- [x] 2.4 将旧静态 specs/ 移至 docs/sdd-legacy/ 作为历史参考

## 3. 质量门禁确认（无代码改动，验证工程仍健康）

- [x] 3.1 后端 `mvn spotless:check` 通过（此前已验证）
- [x] 3.2 前端-web `npm run lint` 与 `npm run format:check` 零报错、Vitest 与 Puppeteer 全绿（此前已验证）
- [x] 3.3 前端-h5 `npm run lint` 与 `npm run format:check` 零报错、Vitest 8 项通过、Puppeteer 冒烟+闭环 17 项全绿

## 4. 校验与归档

- [x] 4.1 运行 `openspec validate init-mall-platform` 确认结构合规
- [x] 4.2 运行 `openspec archive init-mall-platform` 将 delta 同步进 openspec/specs/ 主规格库
- [x] 4.3 在归档摘要中说明触达的需求编号与已知 gap
