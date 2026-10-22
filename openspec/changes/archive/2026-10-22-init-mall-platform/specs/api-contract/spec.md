# Spec Delta

## Purpose

定义青果纪商城前后端接口契约的通用约定，是前后端对接的唯一权威来源；具体端点清单见 docs/sdd-legacy/03-详细设计/01-接口规范.md。

## ADDED Requirements

### Requirement: AC-01 响应体结构
客户端接口 SHALL 统一返回 {code, msg, data}（code=200 成功）；管理端列表 SHALL 返回 {total, rows}（由 getDataTable 输出）。

#### Scenario: 成功响应
- **WHEN** 接口成功
- **THEN** 返回 code=200 与 data 负载

### Requirement: AC-02 鉴权方式
受保护接口 SHALL 经请求头 X-Token 传递令牌；买家接口位于 /api/**，管理端接口位于 /admin-api/**，由 @RequireLogin(LoginType) 拦截器校验。

#### Scenario: 未携带令牌
- **WHEN** 访问受保护接口无 X-Token
- **THEN** 返回 401 并提示

### Requirement: AC-03 错误码与提示
业务异常 SHALL 以 ServiceException("文案") 表达，由全局异常处理转为 {code:500, msg:"文案"}；参数校验失败返回对应错误提示。

#### Scenario: 业务异常
- **WHEN** 业务规则不满足（如库存不足）
- **THEN** 返回 code=500 与可读 msg

### Requirement: AC-04 分页约定
客户端列表 SHALL 返回 Spring Page（前端消费 content/totalElements）；管理端列表 SHALL 返回 {total, rows}。

#### Scenario: 分页返回
- **WHEN** 请求商品列表第一页
- **THEN** 返回分页包装与总条数

### Requirement: AC-05 契约疑问流程
前端对契约有疑问或与实现不符 SHALL 记录在对应 change 的 design.md「契约疑问」章节，由后端确认后更新本能力主规格，禁止前端直接改主规格。

#### Scenario: 契约分歧
- **WHEN** 前端发现接口与文档不一致
- **THEN** 提契约疑问，后端确认后更新 api-contract 主规格

### Requirement: AC-06 端点清单权威
本能力主规格 SHALL 与 docs/sdd-legacy 的接口规范保持内容一致；新增/修改端点须经 change 的 specs delta 同步，不得绕开。

#### Scenario: 新增端点
- **WHEN** 后端新增接口
- **THEN** 经 change 在 api-contract 能力下 ADDED 对应需求并归档
