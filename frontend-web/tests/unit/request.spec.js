import { describe, it, expect } from 'vitest'
import { unwrapPage, unwrapList } from '@/utils/request'
import { toPageParams } from '@/api/page'

describe('utils/request 分页适配', () => {
  const springPage = {
    content: [{ id: 1 }, { id: 2 }],
    totalElements: 12,
    totalPages: 6,
    number: 0,
    size: 2
  }

  it('unwrapPage 把 Spring Data Page 转为 { rows, total }', () => {
    expect(unwrapPage(springPage)).toEqual({ rows: [{ id: 1 }, { id: 2 }], total: 12 })
  })

  it('unwrapPage 兼容已经是 { rows, total } 的结构', () => {
    expect(unwrapPage({ rows: [{ id: 1 }], total: 1 })).toEqual({ rows: [{ id: 1 }], total: 1 })
  })

  it('unwrapPage 对空值返回空列表', () => {
    expect(unwrapPage(null)).toEqual({ rows: [], total: 0 })
    expect(unwrapPage(undefined)).toEqual({ rows: [], total: 0 })
  })

  it('unwrapList 把数组转为 { rows, total }', () => {
    expect(unwrapList([1, 2, 3])).toEqual({ rows: [1, 2, 3], total: 3 })
    expect(unwrapList(null)).toEqual({ rows: [], total: 0 })
  })
})

describe('api/page 分页参数转换', () => {
  it('pageNum 从 1 开始时转成后端从 0 开始的 page', () => {
    expect(toPageParams({ pageNum: 1, pageSize: 10 })).toEqual({ page: 0, size: 10 })
    expect(toPageParams({ pageNum: 3, pageSize: 20 })).toEqual({ page: 2, size: 20 })
  })

  it('保留额外查询条件', () => {
    expect(toPageParams({ pageNum: 2, pageSize: 10, status: 'PAID' })).toEqual({
      page: 1,
      size: 10,
      status: 'PAID'
    })
  })

  it('缺省值兜底为第一页 10 条', () => {
    expect(toPageParams()).toEqual({ page: 0, size: 10 })
  })
})
