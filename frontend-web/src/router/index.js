import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

/**
 * 常驻路由（无需权限，直接注册到 router）
 * meta.title 菜单标题，meta.icon 菜单图标（element-plus 图标组件名）
 */
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login.vue'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404.vue'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/dashboard/index.vue'),
        name: 'Index',
        meta: { title: '首页看板', icon: 'Odometer', affix: true }
      }
    ]
  }
]

/**
 * 业务路由（模拟若依由后端菜单下发的动态路由，登录后由 addRoute 注入）
 * 注意：这里不能再被 createRouter 注册，否则同一 path 会出现重复匹配，
 * 导致 SPA 内部跳转后 router-view 渲染为空。
 */
export const asyncRoutes = [
  {
    path: '/product',
    component: Layout,
    children: [
      {
        path: '',
        component: () => import('@/views/product/index.vue'),
        name: 'Product',
        meta: { title: '商品管理', icon: 'Goods' }
      }
    ]
  },
  {
    path: '/stock',
    component: Layout,
    children: [
      {
        path: '',
        component: () => import('@/views/stock/index.vue'),
        name: 'Stock',
        meta: { title: '库存管理', icon: 'Box' }
      }
    ]
  },
  {
    path: '/order',
    component: Layout,
    children: [
      {
        path: '',
        component: () => import('@/views/order/index.vue'),
        name: 'Order',
        meta: { title: '订单管理', icon: 'Tickets' }
      }
    ]
  },
  {
    path: '/aftersale',
    component: Layout,
    children: [
      {
        path: '',
        component: () => import('@/views/aftersale/index.vue'),
        name: 'AfterSale',
        meta: { title: '售后管理', icon: 'RefreshLeft' }
      }
    ]
  },
  {
    path: '/member',
    component: Layout,
    children: [
      {
        path: '',
        component: () => import('@/views/member/index.vue'),
        name: 'Member',
        meta: { title: '会员管理', icon: 'User' }
      }
    ]
  },
  // 兜底：未匹配的路径统一跳 404（与若依一致，加载动态路由后注册）
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    hidden: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

export default router
