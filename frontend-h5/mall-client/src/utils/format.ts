/**
 * 纯展示格式化工具（无 Taro / 框架依赖，便于单测与多端复用）。
 */

/** 金额格式化：保留两位小数并加货币符号；非法值兜底为 0.00 */
export function formatMoney(value: number | string | null | undefined, symbol = '¥'): string {
  const n = typeof value === 'string' ? parseFloat(value) : Number(value)
  if (!isFinite(n)) return `${symbol}0.00`
  return `${symbol}${n.toFixed(2)}`
}

/** 手机号脱敏：保留前 3 后 4，中间 ****；非 11 位原样返回 */
export function formatPhone(phone: string | null | undefined): string {
  if (!phone) return ''
  const s = String(phone).replace(/\D/g, '')
  if (s.length !== 11) return s
  return `${s.slice(0, 3)}****${s.slice(7)}`
}

/** 时间格式化：后端 ISO（含微秒）转为「YYYY-MM-DD HH:mm:ss」；空值返回占位符 — */
export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '—'
  const m = String(value).match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})/)
  if (m) return `${m[1]}-${m[2]}-${m[3]} ${m[4]}:${m[5]}:${m[6]}`
  return String(value)
}
