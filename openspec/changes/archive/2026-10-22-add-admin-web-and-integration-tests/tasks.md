# Tasks: add-admin-web-and-integration-tests

## 1. Web 管理端
- [x] 1.1 若依风格 admin.html（侧栏/面包屑/搜索区/表格/对话框/分页）
- [x] 1.2 覆盖全部 14 个 /admin-api 接口
- [x] 1.3 后端 static 同源托管，E2E-44 验证可达

## 2. 退货退款全链路 E2E
- [x] 2.1 E2E-36~43（申请→审核→寄回→确认收货→库存回补→积分扣回）
- [x] 2.2 修复缺陷 #10：fillReturnLogistics 空物流单号校验

## 3. H5 联调对账
- [x] 3.1 E2E-45 五页面模块编译可达
- [x] 3.2 E2E-46 api.ts 15 接口契约对账自动化

## 4. 回归与文档
- [x] 4.1 mvnw test 回归 25/25
- [x] 4.2 更新测试用例集/测试报告至 v1.2.0
