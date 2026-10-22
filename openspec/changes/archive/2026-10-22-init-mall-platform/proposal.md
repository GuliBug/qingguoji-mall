# Proposal

## Why

本仓库此前用一份静态 `specs/` 文档集（需求→设计→规范→测试→部署）管理 SDD，存在「与代码脱节、无变更追溯、无校验门禁」三类问题。现采用 OpenSpec（spec-driven schema）将 SDD 转为「change 增量 + 主规格库」的可演进结构，并把现有文档作为基线迁移进主规格库，使需求/设计/契约/任务可回溯、可被后续变更 delta。

## What Changes

- 在仓库内建立 `openspec/`（config.yaml + specs/ + changes/），作为 SDD 的唯一权威来源。
- 将现有需求/设计/规范/测试/部署内容迁移为基线 change `init-mall-platform`，归档后成为主规格库 `openspec/specs/<cap>/spec.md`。
- 旧 `specs/` 文档移至 `docs/sdd-legacy/` 作为历史参考，仓库根不再保留静态 SDD。
- 约定：规划产物中文撰写、`api-contract` 为前后端契约权威、质量门禁（lint/format/test/e2e）作为每个 change 的收尾任务、禁止 AI 自动提交。

## Capabilities

### New Capabilities
- `membership`: 会员、手机号验证码登录、五级等级、消费/签到/扫码积分、积分流水
- `catalog`: 商品分类/列表/详情/SKU、上下架、库存四原语与防超卖
- `cart`: 服务端购物车（加购/查看/改数量/结算金额）
- `order`: 订单创建/库存预占/模拟支付/状态机/取消/列表/确认收货/发货
- `aftersale`: 售后申请/类型/审核/仅退款/退货退款/金额约束
- `web-admin`: Web 管理端页面与接口（看板/商品/库存/订单/售后/会员/管理员登录）
- `h5-client`: H5 客户端工程约定与五个页面入口
- `platform`: 后端若依风格架构、前后端分离边界、双端令牌、环境依赖、部署运维
- `api-contract`: 前后端接口契约约定（响应体/鉴权/错误码/分页/契约疑问流程）

### Modified Capabilities
<!-- 基线初始化，无既有能力被修改 -->

## Impact

- 文档：`specs/` 迁移至 `openspec/specs/`；旧文档归档 `docs/sdd-legacy/`。
- 代码：无代码改动，本次仅迁文档与建立流程。
- 流程：后续新需求/缺陷修复均以 `openspec new change` 起 change，经 propose→apply→verify→archive 管理。
