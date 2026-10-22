/**
 * 金额/日期格式化工具
 */

/**
 * 金额格式化：¥10.00
 * @param {number|string} value
 * @param {string} symbol 货币符号，传 '' 可去掉
 * @returns {string}
 */
export function formatMoney(value, symbol = '¥') {
  const num = Number(value)
  if (Number.isNaN(num)) {
    return symbol + '0.00'
  }
  return symbol + num.toFixed(2)
}

/**
 * 数字格式化，空值展示占位符
 * @param {number|string} value
 * @param {string} placeholder
 * @returns {string|number}
 */
export function formatNumber(value, placeholder = '0') {
  if (value === null || value === undefined || value === '') {
    return placeholder
  }
  return value
}

/**
 * ISO 时间转 yyyy-MM-dd HH:mm:ss
 * @param {string} value 例如 2026-10-22T10:13:38.046996
 * @returns {string}
 */
export function formatDateTime(value) {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return String(value)
  }
  const pad = (n) => (n < 10 ? '0' + n : String(n))
  return (
    date.getFullYear() +
    '-' +
    pad(date.getMonth() + 1) +
    '-' +
    pad(date.getDate()) +
    ' ' +
    pad(date.getHours()) +
    ':' +
    pad(date.getMinutes()) +
    ':' +
    pad(date.getSeconds())
  )
}
