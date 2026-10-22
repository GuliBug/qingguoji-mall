import { describe, it, expect } from 'vitest'
import { isMobile, isEmpty, isPathMatch, isHttp, isExternal, validPassword } from '@/utils/validate'
import { parseTime, tansParams, selectDictLabel } from '@/utils/ruoyi'
import { dictLabel, ORDER_STATUS, MEMBER_LEVEL } from '@/utils/dict'

describe('utils/validate', () => {
  it('isMobile 校验中国大陆手机号', () => {
    expect(isMobile('13800001111')).toBe(true)
    expect(isMobile('12345')).toBe(false)
    expect(isMobile('23800001111')).toBe(false)
    expect(isMobile('')).toBe(false)
  })

  it('isEmpty 覆盖 null/空串/空数组/空对象', () => {
    expect(isEmpty(null)).toBe(true)
    expect(isEmpty(undefined)).toBe(true)
    expect(isEmpty('')).toBe(true)
    expect(isEmpty([])).toBe(true)
    expect(isEmpty({})).toBe(true)
    expect(isEmpty(0)).toBe(false)
    expect(isEmpty('a')).toBe(false)
    expect(isEmpty([1])).toBe(false)
  })

  it('isPathMatch 支持通配', () => {
    expect(isPathMatch('/login', '/login')).toBe(true)
    expect(isPathMatch('/login', '/index')).toBe(false)
    expect(isPathMatch('/a/*', '/a/b')).toBe(true)
    expect(isPathMatch('/a/*', '/b/b')).toBe(false)
  })

  it('isHttp / isExternal 区分站内外链接', () => {
    expect(isHttp('http://localhost:8080')).toBe(true)
    expect(isHttp('/index')).toBe(false)
    expect(isExternal('https://ruoyi.vip')).toBe(true)
    expect(isExternal('mailto:a@b.com')).toBe(true)
    expect(isExternal('/index')).toBe(false)
  })

  it('validPassword 长度 5~20', () => {
    expect(validPassword('admin123')).toBe(true)
    expect(validPassword('123')).toBe(false)
  })
})

describe('utils/ruoyi', () => {
  it('parseTime 支持自定义格式', () => {
    expect(parseTime('2026-10-22 10:13:38', '{y}/{m}/{d}')).toBe('2026/10/22')
    expect(parseTime('2026-10-22T10:13:38', '{h}:{i}:{s}')).toBe('10:13:38')
    expect(parseTime(null)).toBe(null)
  })

  it('parseTime 能解析后端带微秒的 ISO 串', () => {
    expect(parseTime('2026-10-22T10:13:38.046996', '{y}-{m}-{d} {h}:{i}:{s}')).toBe('2026-10-22 10:13:38')
  })

  it('tansParams 拼接查询参数并跳过空值', () => {
    expect(tansParams({ page: 0, size: 10 })).toBe('page=0&size=10&')
    expect(tansParams({ a: 1, b: '', c: null })).toBe('a=1&')
  })

  it('selectDictLabel 命中返回值对应标签', () => {
    const dict = [
      { value: 1, label: '在售' },
      { value: 0, label: '已下架' }
    ]
    expect(selectDictLabel(dict, 1)).toBe('在售')
    expect(selectDictLabel(dict, 0)).toBe('已下架')
    expect(selectDictLabel(dict, 9, '未知')).toBe('未知')
  })
})

describe('utils/dict', () => {
  it('dictLabel 命中返回标签与颜色类型', () => {
    expect(dictLabel(ORDER_STATUS, 'PAID')).toEqual({ label: '已付款', type: 'primary' })
    expect(dictLabel(ORDER_STATUS, 'COMPLETED')).toEqual({ label: '已完成', type: 'success' })
  })

  it('dictLabel 未命中时回退为 info 并原样展示', () => {
    expect(dictLabel(ORDER_STATUS, 'UNKNOWN')).toEqual({ label: 'UNKNOWN', type: 'info' })
    expect(dictLabel(ORDER_STATUS, null)).toEqual({ label: '—', type: 'info' })
  })

  it('会员等级字典覆盖后端 0~4', () => {
    expect(MEMBER_LEVEL.map((i) => i.value)).toEqual([0, 1, 2, 3, 4])
    expect(dictLabel(MEMBER_LEVEL, 4).label).toBe('黑金卡')
  })
})
