# Spec Delta

## Purpose

面向消费者的移动端（Taro 4 + Vue3 + TypeScript，编译目标 H5/微信小程序），承载浏览、加购、下单、支付、售后、会员中心五个页面。

## ADDED Requirements

### Requirement: H5-01 后端调用统一出口
所有 H5 后端调用 SHALL 经 src/api.ts 封装，禁止页面内直接 Taro.request；返回判断 res.code === 200 成功否则 res.msg 提示。

#### Scenario: 统一调用
- **WHEN** 页面需要请求后端
- **THEN** 通过 api.ts 发起并统一处理 code/msg

### Requirement: H5-02 登录态存储
H5 SHALL 将登录令牌存于 Taro.setStorageSync，页面以 ref 局部管理状态。

#### Scenario: 登录后持久化
- **WHEN** 买家登录成功
- **THEN** 令牌持久化，刷新后仍保持登录态

### Requirement: H5-03 五个页面与 tabBar
H5 SHALL 提供首页、商品详情、购物车、订单、我的五个页面，并经 app.config.ts 的 pages 注册；底部 tabBar 含首页/购物车/我的/订单。

#### Scenario: tabBar 导航
- **WHEN** 买家点击底部 tab
- **THEN** 真实切换路由并触发对应页面的 useDidShow 加载

### Requirement: H5-04 演示登录流程
H5 SHALL 在首页提供一键登录（演示手机号 13800001111 / 验证码 123456），经 Taro.showModal 确认后登录。

#### Scenario: 一键登录
- **WHEN** 买家点击一键登录并确认
- **THEN** 调用登录接口并进入会员态

### Requirement: H5-05 下单闭环
H5 SHALL 支持从详情页加购、购物车提交订单、模拟支付、跳转订单页查看订单。

#### Scenario: 购物闭环
- **WHEN** 买家加购并提交支付
- **THEN** 订单出现在订单列表，状态为已支付

### Requirement: H5-06 类型与构建校验
H5 SHALL 通过 npm run build:h5 通过以代表 TypeScript 类型与编译无误，并纳入 ESLint/Prettier/Vitest/Puppeteer 质量门禁。

#### Scenario: 构建通过
- **WHEN** 执行 build:h5
- **THEN** 无类型/编译错误
