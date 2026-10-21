# 项目上下文（Project Context）

## 项目概述

青果纪 · 槟榔品牌会员商城 H5：品牌商品售卖 + 五级会员体系（成长值/积分/签到/扫码积分）+ 完整购物链路（加购→下单→支付→发货→售后）。对标正隆记小程序。

## 技术栈

- **后端** `backend/`：Java 21 · Spring Boot 3.5.6 · Spring Data JPA · H2（MODE=MySQL，生产预留 MySQL profile）
- **前端** `frontend-h5/mall-client/`：Taro 4.3 · Vue 3 · TypeScript · Vite 4.5（编译目标 H5，预留小程序端）
- **鉴权**：请求头 `X-Token`（买家 / 管理员两套独立令牌）

## 当前能力（Capabilities）

| 能力规格 | 状态 | 说明 |
|---|---|---|
| specs/会员积分模块 | 已归档 | 登录注册、五级等级、签到、扫码积分、流水 |
| specs/商品库存模块 | 已归档 | 商品/SKU、库存预占-扣减-释放-回补 |
| specs/订单模块 | 已归档 | 下单、支付、取消、超时、发货收货状态机 |
| specs/售后模块 | 已归档 | 仅退款/退货退款、审核流、积分扣回 |
| specs/管理端模块 | 已归档 | 独立登录、看板、商品/库存/订单/售后/会员管理 |
| specs/前端H5客户端 | 已归档 | 6 大页面模块与登录态封装 |
| specs/前后端联调 | 已归档 | 接口对账、错误处理约定、冒烟验收 |
| specs/参数校验模块 | 已归档 | 全接口字段级校验（空值/格式/边界/枚举） |

## 约定

- 模块/能力命名使用中文；代码标识符用英文。
- 需求编号沿用 `specs/spec.md` 的 U/P/C/O/S/A/V/M 体系，Requirement 中可引用。
- 验收标准一律用 Scenario（WHEN/THEN）表达。
- 每次 change 走 proposal → tasks → spec deltas → 实现 → archive 流程，归档至 `changes/archive/<日期>-<change-id>/`。
