import Taro from '@tarojs/taro'

/**
 * 后端接口封装：base 指向 Spring Boot 后端。
 * 登录态 token 存本地，请求自动携带 X-Token。
 *
 * 地址策略（免改代码即可上服务器）：
 * - 本地开发（localhost / 127.0.0.1）直连 8080；
 * - 线上走 Nginx 同域反代，根地址留空，请求自然打成 /api/xxx。
 */
function resolveBase(): string {
  if (typeof window === 'undefined') {
    // 小程序端无 window，需在配置里指定完整域名
    return ''
  }
  const host = window.location.hostname
  return host === 'localhost' || host === '127.0.0.1' ? 'http://localhost:8080' : ''
}

const BASE = resolveBase()

const TOKEN_KEY = 'qgj_token'

export function getToken(): string {
  return Taro.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(t: string) {
  Taro.setStorageSync(TOKEN_KEY, t)
}

export function isLoggedIn(): boolean {
  return !!getToken()
}

async function request<T = any>(method: 'GET' | 'POST' | 'PUT', path: string, data?: any): Promise<T> {
  const res = await Taro.request({
    url: BASE + path,
    method,
    data,
    header: { 'Content-Type': 'application/json', 'X-Token': getToken() }
  })
  return res.data as T
}

// ---- 会员 ----
export const api = {
  login: (phone: string, smsCode: string) => request('POST', '/api/member/login', { phone, smsCode }),
  me: () => request('GET', '/api/member/me'),
  checkin: () => request('POST', '/api/member/checkin'),
  scan: (code: string) => request('POST', '/api/member/scan', { code }),
  pointsLogs: () => request('GET', '/api/member/points/logs'),

  // ---- 商品 / 购物车 ----
  products: (page = 0, category = '') =>
    request('GET', `/api/products?page=${page}&size=20${category ? '&category=' + category : ''}`),
  productDetail: (id: number) => request('GET', `/api/products/${id}`),
  cart: () => request('GET', '/api/cart'),
  addToCart: (skuId: number, qty: number) => request('POST', '/api/cart', { skuId, qty }),
  updateCart: (cartId: number, qty: number) => request('PUT', `/api/cart/${cartId}`, { qty }),

  // ---- 订单 / 售后 ----
  createOrder: (lines: { skuId: number; qty: number }[], receiver: string, receiverPhone: string, address: string) =>
    request('POST', '/api/orders', { lines, receiver, receiverPhone, address }),
  pay: (orderNo: string) => request('POST', `/api/orders/${orderNo}/pay`),
  cancel: (orderNo: string) => request('POST', `/api/orders/${orderNo}/cancel`),
  confirm: (orderNo: string) => request('POST', `/api/orders/${orderNo}/confirm`),
  orders: (status = '') => request('GET', `/api/orders?size=20${status ? '&status=' + status : ''}`),
  applyAfterSale: (orderItemId: number, type: string, qty: number, reason: string) =>
    request('POST', '/api/aftersale', { orderItemId, type, qty, reason, description: '' }),
  myAfterSales: () => request('GET', '/api/aftersale/my')
}
