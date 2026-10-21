export default defineAppConfig({
  pages: [
    'pages/index/index',
    'pages/product/index',
    'pages/member/index',
    'pages/cart/index',
    'pages/orders/index'
  ],
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#B0231F',
    navigationBarTitleText: '青果纪',
    navigationBarTextStyle: 'white'
  },
  tabBar: {
    color: '#9A8F86',
    selectedColor: '#B0231F',
    backgroundColor: '#ffffff',
    list: [
      { pagePath: 'pages/index/index', text: '首页' },
      { pagePath: 'pages/cart/index', text: '购物车' },
      { pagePath: 'pages/member/index', text: '我的' },
      { pagePath: 'pages/orders/index', text: '订单' }
    ]
  }
})
