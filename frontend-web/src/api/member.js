import request, { unwrapPage } from '@/utils/request'
import { toPageParams } from '@/api/page'

/**
 * 查询会员列表
 * @param {object} query 查询条件 { pageNum, pageSize }
 * @returns {Promise<{ rows: Array, total: number }>}
 */
export function listMember(query) {
  return request({
    url: '/members',
    method: 'get',
    params: toPageParams(query)
  }).then((res) => unwrapPage(res.data))
}

/**
 * 积分人工调整（留痕）
 * @param {number} id 会员 ID
 * @param {object} data { delta, reason }
 * @returns {Promise<{ points: number }>}
 */
export function adjustPoints(id, data) {
  return request({
    url: '/members/' + id + '/points',
    method: 'post',
    data
  })
}

/**
 * 查询会员积分流水
 * @param {number} id 会员 ID
 * @param {object} query { pageNum, pageSize }
 * @returns {Promise<{ rows: Array, total: number }>}
 */
export function listPointsLog(id, query) {
  return request({
    url: '/members/' + id + '/points/logs',
    method: 'get',
    params: toPageParams(query)
  }).then((res) => unwrapPage(res.data))
}
