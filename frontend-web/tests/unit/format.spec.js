import { describe, it, expect } from 'vitest'
import { formatMoney, formatNumber, formatDateTime } from '@/utils/format'

describe('utils/format', () => {
  describe('formatMoney', () => {
    it('保留两位小数并带上货币符号', () => {
      expect(formatMoney(10)).toBe('¥10.00')
      expect(formatMoney(10.5)).toBe('¥10.50')
      expect(formatMoney('99.999')).toBe('¥100.00')
    })

    it('空值与非法值兜底为 0.00', () => {
      expect(formatMoney(null)).toBe('¥0.00')
      expect(formatMoney(undefined)).toBe('¥0.00')
      expect(formatMoney('abc')).toBe('¥0.00')
    })

    it('支持自定义货币符号', () => {
      expect(formatMoney(5, '')).toBe('5.00')
      expect(formatMoney(5, '$')).toBe('$5.00')
    })
  })

  describe('formatNumber', () => {
    it('正常值原样返回', () => {
      expect(formatNumber(3)).toBe(3)
    })

    it('空值返回占位符', () => {
      expect(formatNumber(null)).toBe('0')
      expect(formatNumber('', '-')).toBe('-')
    })
  })

  describe('formatDateTime', () => {
    it('格式化后端返回的 ISO 时间（含微秒）', () => {
      expect(formatDateTime('2026-10-22T10:13:38.046996')).toBe('2026-10-22 10:13:38')
    })

    it('无值时返回占位符', () => {
      expect(formatDateTime(null)).toBe('—')
      expect(formatDateTime('')).toBe('—')
    })

    it('非法时间原样返回字符串', () => {
      expect(formatDateTime('not-a-date')).toBe('not-a-date')
    })
  })
})
