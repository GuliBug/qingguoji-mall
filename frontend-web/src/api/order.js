import request, { unwrapPage } from '@/utils/request'
import { toPageParams } from '@/api/page'

/**
 * 查询订单列表
 * @param {object} query 查询条件 { pageNum, pageSize, status }
 * @returns {Promise<{ rows: Array, total: number }>}
 */
export function listOrder(query) {
  return request({
    url: '/orders',
    method: 'get',
    params: toPageParams(query)
  }).then((res) => unwrapPage(res.data))
}

/**
 * 订单发货
 * @param {string} orderNo 订单号
 * @param {string} logisticsNo 物流单号
 * @returns {Promise}
 */
export function shipOrder(orderNo, logisticsNo) {
  return request({
    url: '/orders/' + orderNo + '/ship',
    method: 'post',
    data: { logisticsNo }
  })
}
