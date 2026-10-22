import { defineStore } from 'pinia'
import { constantRoutes, asyncRoutes } from '@/router'

/**
 * 权限路由 store
 * 说明：后端暂未提供菜单接口（RuoYi 原版由 /getRouters 动态下发），
 * 这里用前端 asyncRoutes 模拟动态菜单。
 * 后端补上菜单接口后，只需把 generateRoutes 改为请求接口并过滤角色即可，调用方无需改动。
 */
const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    addRoutes: [],
    sidebarRouters: []
  }),
  actions: {
    /**
     * 生成可访问路由
     * @returns {Promise<Array>}
     */
    generateRoutes() {
      return new Promise((resolve) => {
        const accessedRoutes = [...asyncRoutes]
        this.addRoutes = accessedRoutes
        // 侧边栏 = 常驻路由（首页）+ 动态路由
        this.routes = constantRoutes.concat(accessedRoutes)
        this.sidebarRouters = constantRoutes.concat(accessedRoutes)
        resolve(accessedRoutes)
      })
    }
  }
})

export default usePermissionStore
