import request, { unwrapPage } from '@/utils/request'
import { toPageParams } from '@/api/page'

/**
 * 查询售后列表
 * @param {object} query 查询条件 { pageNum, pageSize, status }
 * @returns {Promise<{ rows: Array, total: number }>}
 */
export function listAfterSale(query) {
  return request({
    url: '/aftersales',
    method: 'get',
    params: toPageParams(query)
  }).then((res) => unwrapPage(res.data))
}

/**
 * 售后审核
 * @param {number} id 售后 ID
 * @param {object} data { pass, rejectReason }
 * @returns {Promise}
 */
export function auditAfterSale(id, data) {
  return request({
    url: '/aftersales/' + id + '/audit',
    method: 'post',
    data
  })
}

/**
 * 确认收到退货并退款
 * @param {number} id 售后 ID
 * @returns {Promise}
 */
export function confirmReturn(id) {
  return request({
    url: '/aftersales/' + id + '/confirm-return',
    method: 'post'
  })
}
