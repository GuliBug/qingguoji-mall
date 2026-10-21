# 提案：add-frontend-spec 新增前端 H5 客户端能力规格

## Why（动机）

前端 Taro + Vue3 H5 已落地 6 个页面模块并与后端联调通过，需要将前端行为约束成规格，作为后续 UI 迭代与 E2E 用例的基线。

## What Changes（变更内容）

新增 1 个能力规格 delta（ADDED）：**前端H5客户端**，覆盖网络与登录态封装、6 大页面模块的交互行为与异常处理约定。

## Impact（影响范围）

- 影响文件：`openspec/specs/前端H5客户端/spec.md`（归档时创建）
- 约束对象：`frontend-h5/mall-client/src/` 下 api.ts 与 pages/**
