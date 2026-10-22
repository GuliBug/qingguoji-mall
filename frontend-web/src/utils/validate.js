/**
 * 通用校验工具（与若依 utils/validate.js 保持一致的习惯用法）
 */

/**
 * 验证是否为 http(s) 链接
 * @param {string} path
 * @returns {boolean}
 */
export function isHttp(path) {
  return (
    /^https?:\/\/.+/.test(path) ||
    /^http(s?):\/\/(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*(),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+$/.test(path)
  )
}

/**
 * 验证是否为外链（非 http 开头的视为内部路由）
 * @param {string} path
 * @returns {boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * 是否为空白（null / undefined / '' / [] / {}）
 * @param {*} value
 * @returns {boolean}
 */
export function isEmpty(value) {
  if (value === null || value === undefined || value === '') {
    return true
  }
  if (Array.isArray(value) && value.length === 0) {
    return true
  }
  if (typeof value === 'object' && Object.keys(value).length === 0) {
    return true
  }
  return false
}

/**
 * 手机号校验（中国大陆）
 * @param {string} value
 * @returns {boolean}
 */
export function isMobile(value) {
  return /^1[3-9]\d{9}$/.test(String(value || '').trim())
}

/**
 * 简易路径匹配，支持 /a/* 形式的通配
 * @param {string} pattern
 * @param {string} path
 * @returns {boolean}
 */
export function isPathMatch(pattern, path) {
  if (!pattern) return false
  if (!pattern.includes('*')) return pattern === path
  const reg = new RegExp('^' + pattern.replace(/\*/g, '.*') + '$')
  return reg.test(path)
}

/**
 * 校验密码强度：长度 5~20
 * @param {string} value
 * @returns {boolean}
 */
export function validPassword(value) {
  const str = String(value || '')
  return str.length >= 5 && str.length <= 20
}
