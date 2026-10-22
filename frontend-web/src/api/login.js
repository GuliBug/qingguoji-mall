import request from '@/utils/request'

/**
 * 管理员登录
 * @param {string} username 账号
 * @param {string} password 密码
 * @returns {Promise<{ token: string }>}
 */
export function login(username, password) {
  const data = { username, password }
  return request({
    url: '/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: data
  })
}

/**
 * 校验登录态
 * 说明：后端当前未提供 /admin-api/getInfo 接口，这里借用 /dashboard 校验 token 是否有效。
 * 后续若新增 getInfo，把 url 换成 '/getInfo' 即可，返回结构保持 { data } 不变。
 * @returns {Promise<object>}
 */
export function checkToken() {
  return request({
    url: '/dashboard',
    headers: { repeatSubmit: false },
    method: 'get'
  })
}

/**
 * 退出登录
 * 说明：后端为内存版 token，没有 /logout 接口，前端仅清理本地凭证。
 * @returns {Promise<boolean>}
 */
export function logout() {
  return Promise.resolve(true)
}
