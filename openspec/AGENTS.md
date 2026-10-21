# OpenSpec 工作流说明

1. **提案**：在 `changes/<change-id>/` 创建 `proposal.md`（动机/变更内容/影响范围）、`tasks.md`（可勾选任务清单）、`specs/<能力>/spec.md`（规格增量，用 `## ADDED/MODIFIED/REMOVED Requirements` 标注）。
2. **实现**：按 tasks.md 逐项实现并勾选；测试用例从 Scenario 派生。
3. **归档**：实现与验证完成后，将 deltas 合并进 `openspec/specs/<能力>/spec.md`，change 文件夹整体移入 `changes/archive/<YYYY-MM-DD>-<change-id>/`。
4. **格式约定**：Requirement 用 `### Requirement: 名称` + SHALL 陈述；Scenario 用 `#### Scenario: 名称` + `- **WHEN** / **THEN**` 列表。
