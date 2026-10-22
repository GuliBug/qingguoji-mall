import { defineStore } from 'pinia'
import { login, logout, checkToken } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'

const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    name: '',
    avatar: '',
    roles: [],
    permissions: []
  }),
  actions: {
    /**
     * 登录
     * @param {object} userInfo { username, password }
     * @returns {Promise<void>}
     */
    login(userInfo) {
      const username = userInfo.username.trim()
      const password = userInfo.password
      return new Promise((resolve, reject) => {
        login(username, password)
          .then((res) => {
            setToken(res.data.token)
            this.token = res.data.token
            resolve()
          })
          .catch((error) => {
            reject(error)
          })
      })
    },
    /**
     * 获取用户信息
     * 说明：后端当前没有 /admin-api/getInfo，也没有菜单接口，
     * 因此角色固定为 admin、菜单走前端静态路由；
     * 这里用 /dashboard 做一次 token 有效性校验，避免 token 失效后页面空白。
     * @returns {Promise<object>}
     */
    getInfo() {
      return new Promise((resolve, reject) => {
        checkToken()
          .then(() => {
            this.roles = ['admin']
            this.permissions = ['*:*:*']
            this.name = 'admin'
            this.avatar = ''
            resolve({ roles: this.roles, permissions: this.permissions })
          })
          .catch((error) => {
            reject(error)
          })
      })
    },
    /**
     * 退出系统
     * @returns {Promise<void>}
     */
    logOut() {
      return new Promise((resolve, reject) => {
        logout()
          .then(() => {
            this.token = ''
            this.roles = []
            this.permissions = []
            removeToken()
            resolve()
          })
          .catch((error) => {
            reject(error)
          })
      })
    }
  }
})

export default useUserStore
