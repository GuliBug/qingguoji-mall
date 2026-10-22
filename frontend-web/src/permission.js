import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login']

/**
 * 全局前置守卫（若依风格：NProgress + 白名单 + 动态路由）
 */
router.beforeEach(async (to) => {
  NProgress.start()
  if (getToken()) {
    if (to.path === '/login') {
      NProgress.done()
      return { path: '/' }
    }
    if (whiteList.some((pattern) => isPathMatch(pattern, to.path))) {
      return true
    }
    if (useUserStore().roles.length === 0) {
      isRelogin.show = true
      try {
        // 拉取用户信息
        await useUserStore().getInfo()
        isRelogin.show = false
        // 根据 roles 权限生成可访问的路由
        const accessRoutes = await usePermissionStore().generateRoutes()
        accessRoutes.forEach((route) => {
          router.addRoute(route)
        })
        // 重新导航到目标路由，确保动态路由已注册
        return { ...to, replace: true }
      } catch (err) {
        await useUserStore().logOut()
        ElMessage.error(err && err.message ? err.message : '登录状态已失效，请重新登录')
        return { path: '/login' }
      }
    }
    return true
  }
  // 没有 token
  if (whiteList.some((pattern) => isPathMatch(pattern, to.path))) {
    // 在免登录白名单，直接进入
    return true
  }
  NProgress.done()
  return `/login?redirect=${to.fullPath}` // 否则全部重定向到登录页
})

router.afterEach(() => {
  NProgress.done()
})
