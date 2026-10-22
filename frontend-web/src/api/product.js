import request, { unwrapPage } from '@/utils/request'
import { toPageParams } from '@/api/page'

/**
 * 查询商品列表（含已下架）
 * @param {object} query 查询条件 { pageNum, pageSize }
 * @returns {Promise<{ rows: Array, total: number }>}
 */
export function listProduct(query) {
  return request({
    url: '/products',
    method: 'get',
    params: toPageParams(query)
  }).then((res) => unwrapPage(res.data))
}

/**
 * 商品上下架
 * @param {number} id 商品 ID
 * @param {number} status 1 上架 / 0 下架
 * @returns {Promise}
 */
export function changeProductStatus(id, status) {
  return request({
    url: '/products/' + id + '/status',
    method: 'post',
    data: { status }
  })
}
