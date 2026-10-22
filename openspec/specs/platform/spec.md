# platform Specification

## Purpose
定义青果纪商城的整体工程结构、后端分层架构、前后端分离边界、双端令牌机制、环境依赖与部署运维约定，为后续需求变更提供可演进的能力基线与工程约束。

## Requirements

### Requirement: PL-01 工程边界
仓库 SHALL 划分为 backend/（Spring Boot 独立 jar，8080）、frontend-web/（5173）、frontend-h5/（10086）、deploy/、docs/；后端不托管任何前端页面，前后端经 HTTP 契约解耦。

#### Scenario: 后端不托管页面
- **WHEN** 部署后端服务
- **THEN** 不返回任何 HTML 页面，仅提供 /api/** 与 /admin-api/**

### Requirement: PL-02 后端分层
后端 SHALL 采用若依风格三层：common（AjaxResult/TableDataInfo/ServiceException/BaseController）、framework（TokenService/LoginInterceptor/@RequireLogin/定时任务/演示数据初始化）、mall（controller/service/repository/domain）。

#### Scenario: 业务在 service 层
- **WHEN** 实现业务逻辑
- **THEN** 不写在 Controller，统一由 service/impl 承载

### Requirement: PL-03 统一响应与异常
后端 SHALL 以 AjaxResult<T> 返回客户端、TableDataInfo<T> 返回管理端列表；业务异常抛 ServiceException 由 GlobalExceptionHandler 兜底为 {code,msg}。

#### Scenario: 异常兜底
- **WHEN** 业务抛出异常
- **THEN** 返回统一 {code:500, msg} 结构

### Requirement: PL-04 双端令牌
系统 SHALL 使用两套互不通用令牌：买家 MBR-、管理员 ADM-，均经请求头 X-Token 传递；拦截器按 @RequireLogin(LoginType) 校验。

#### Scenario: 令牌越权被拦
- **WHEN** 用 MBR- 令牌访问 /admin-api/**
- **THEN** 拦截器拒绝

### Requirement: PL-05 代码格式化
后端 SHALL 以 Spotless + google-java-format（AOSP，4 空格）强制格式化，mvn verify 时 spotless:check 不通过则构建失败；前端 SHALL 以 ESLint 9 + Prettier 强制格式。

#### Scenario: 格式化门禁
- **WHEN** 提交代码前
- **THEN** 后端 spotless:check、前端 lint/format:check 零报错

### Requirement: PL-06 环境依赖
本机 SHALL 具备 JDK 21、Maven 3.9.6、Node 22，npm 使用 https 源 registry.npmmirror.com；一键启动 start-all.cmd，生产打包 deploy/build.cmd。

#### Scenario: 一键启动
- **WHEN** 执行 start-all.cmd
- **THEN** 依次拉起后端 8080、Web 5173、H5 10086

### Requirement: PL-07 部署运维
生产 SHALL 以 Docker（backend/web） + nginx 反代 + MySQL 持久化部署，文档见 deploy/ 与 DEPLOY.md；提供 docker-compose 与 systemd 两种形态。

#### Scenario: 生产部署
- **WHEN** 按 DEPLOY.md 执行
- **THEN** 可复现地部署后端、Web、H5 与 MySQL
