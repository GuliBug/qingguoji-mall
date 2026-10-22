# AGENTS.md — 青果纪商城 OpenSpec 协作约定

本仓库用 **OpenSpec（spec-driven schema）** 管理 SDD 流程与代码。规格权威来源是 `openspec/`，旧的静态文档已归档至 `docs/sdd-legacy/`。

> 本文件是约定摘要；可执行的规则以 `openspec/config.yaml` 为准（context / rules / operations）。

## 0. 工作流（每个需求/缺陷/重构都走 change）

1. **起 change**：`openspec new change "<kebab-name>"`（例如 `add-coupon`、`fix-stock-overflow`）。
2. **写产物**（按依赖顺序）：`proposal.md` → `specs/<cap>/spec.md`（增量 delta）→ `design.md` → `tasks.md`。
   - 用 `openspec instructions <artifact> --change "<name>" --json` 取模板与规则。
   - 用 `openspec status --change "<name>" --json` 看哪些产物就绪。
3. **实现（apply）**：仅在你明确进入实现阶段后改代码；先读 `openspec/specs/<cap>/spec.md` 作为当前事实。
4. **校验（verify）**：本地跑质量门禁，全绿再勾任务。
5. **归档（archive）**：`openspec archive "<name>" --yes` 把 delta 同步进主规格库并移入 `openspec/changes/archive/`。

> 规划与实现是两段：写完规划产物先停下等用户确认，再进入 apply。

## 1. 产物语言与风格

- 所有规划产物用**中文**撰写；技术标识符（类名/接口路径/字段）保留英文原文。
- `specs` 用 `### Requirement:` + `#### Scenario:`（WHEN/THEN）描述**可观测行为**，不写实现细节；保留原 SRS 编号（如 `### Requirement: U-03 ...`）作为追溯锚点。
- 增量用 `## ADDED/MODIFIED/REMOVED/RENAMED Requirements` 标记；`MODIFIED` 须带完整更新内容。

## 2. 契约权威

- `openspec/specs/api-contract/spec.md` + `docs/sdd-legacy/03-详细设计/01-接口规范.md` 是前后端**唯一契约来源**。
- 前端发现接口与契约不符 → 在对应 change 的 `design.md`「契约疑问」章节记录，后端确认后更新 `api-contract` 主规格；**禁止前端直接改主规格**。

## 3. 质量门禁（每个 change 的收尾任务必须包含）

- 后端：`mvn spotless:check`（在 `backend/`）。
- 前端-web / 前端-h5：`npm run lint` 与 `npm run format:check` 零报错。
- 自动化测试：web/h5 的 `npm test`（Vitest）、`npm run test:e2e`（Puppeteer）全绿。

## 4. 提交纪律

- **禁止 AI 自动 commit / push**；格式化与测试门禁在本地通过后，由人工决定是否提交。
- 提交信息建议 `[模块] 动作：说明`（如 `[backend] refactor: 拆分 MallService`）。

## 5. 工具

- 本机已装 `openspec` CLI（v1.14.3）。`.codebuddy/` 下的 skills/commands 是 OpenSpec 工作流的自动化封装（面向 CodeBuddy CLI）；在本环境直接调用 `openspec` CLI 即可。
- 现有工程边界：`backend/`（Spring Boot，8080）、`frontend-web/`（5173）、`frontend-h5/mall-client/`（10086）、`deploy/`、`docs/`、`openspec/`；后端不托管前端页面。
