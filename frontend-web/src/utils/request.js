import axios from 'axios'
import { ElNotification, ElMessageBox, ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { tansParams } from '@/utils/ruoyi'
import cache from '@/plugins/cache'
import useUserStore from '@/store/modules/user'

// 是否显示重新登录
export let isRelogin = { show: false }

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

// 创建 axios 实例
const service = axios.create({
  // axios 中请求配置有 baseURL 选项，表示请求 URL 公共部分
  baseURL: import.meta.env.VITE_APP_BASE_API,
  // 超时
  timeout: 10000
})

// request 拦截器
service.interceptors.request.use(
  (config) => {
    // 是否需要设置 token
    const isToken = (config.headers || {}).isToken === false
    // 是否需要防止数据重复提交
    const isRepeatSubmit = (config.headers || {}).repeatSubmit === false
    // 间隔时间(ms)，小于此时间视为重复提交
    const interval = (config.headers || {}).interval || 1000
    if (getToken() && !isToken) {
      // 本项目后端使用 X-Token 头（若依原版为 Authorization: Bearer {token}）
      config.headers['X-Token'] = getToken()
    }
    // get 请求映射 params 参数
    if (config.method === 'get' && config.params) {
      let url = config.url + '?' + tansParams(config.params)
      url = url.slice(0, -1)
      config.params = {}
      config.url = url
    }
    if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
      const requestObj = {
        url: config.url,
        data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
        time: new Date().getTime()
      }
      const sessionObj = cache.session.getJSON('sessionObj')
      if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
        cache.session.setJSON('sessionObj', requestObj)
      } else {
        const sUrl = sessionObj.url // 请求地址
        const sData = sessionObj.data // 请求数据
        const sTime = sessionObj.time // 请求时间
        if (sData === requestObj.data && requestObj.time - sTime < interval && sUrl === requestObj.url) {
          const message = '数据正在处理，请勿重复提交'
          console.warn(`[${sUrl}]: ` + message)
          return Promise.reject(new Error(message))
        } else {
          cache.session.setJSON('sessionObj', requestObj)
        }
      }
    }
    return config
  },
  (error) => {
    console.log(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (res) => {
    // 未设置状态码则默认成功状态
    const code = res.data.code || 200
    // 获取错误信息
    const msg = errorCode[code] || res.data.msg || errorCode['default']
    // 二进制数据则直接返回
    if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
      return res.data
    }
    if (code === 401) {
      if (!isRelogin.show) {
        isRelogin.show = true
        ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            isRelogin.show = false
            useUserStore()
              .logOut()
              .then(() => {
                location.href = '/index'
              })
          })
          .catch(() => {
            isRelogin.show = false
          })
      }
      return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
    } else if (code === 500) {
      ElMessage({ message: msg, type: 'error' })
      return Promise.reject(new Error(msg))
    } else if (code !== 200) {
      ElNotification.error({ title: msg })
      return Promise.reject(new Error(msg))
    } else {
      return Promise.resolve(res.data)
    }
  },
  (error) => {
    console.log('err' + error)
    let { message } = error
    if (message === 'Network Error') {
      message = '后端接口连接异常'
    } else if (message.includes('timeout')) {
      message = '系统接口请求超时'
    } else if (message.includes('Request failed with status code')) {
      message = '系统接口' + message.slice(-3) + '异常'
    }
    ElMessage({ message: message, type: 'error', duration: 5 * 1000 })
    return Promise.reject(error)
  }
)

/**
 * 把后端 Spring Data Page 结构转成若依习惯的 { rows, total }
 * 后端返回：{ content: [], totalElements, totalPages, number, size }
 * @param {object} page Spring Page 对象
 * @returns {{ rows: Array, total: number }}
 */
export function unwrapPage(page) {
  if (!page) {
    return { rows: [], total: 0 }
  }
  // 兼容已经是 { rows, total } 的返回
  if (Array.isArray(page.rows)) {
    return { rows: page.rows, total: page.total || page.rows.length }
  }
  return {
    rows: page.content || [],
    total: page.totalElements || 0
  }
}

/**
 * 把后端数组转成若依习惯的 { rows, total }
 * @param {Array} list
 * @returns {{ rows: Array, total: number }}
 */
export function unwrapList(list) {
  const rows = Array.isArray(list) ? list : []
  return { rows, total: rows.length }
}

/**
 * 通用下载（若依 utils/request.js 同名方法，本项目暂无导出接口，保留以备扩展）
 * @param {string} url 请求地址
 * @param {object} params 查询参数
 * @param {string} filename 保存文件名
 */
export function download(url, params, filename) {
  return service
    .get(url, { params, responseType: 'blob' })
    .then((blob) => {
      const href = window.URL.createObjectURL(new Blob([blob]))
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = href
      link.setAttribute('download', filename || 'download')
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(href)
      return true
    })
    .catch(() => false)
}

export default service
