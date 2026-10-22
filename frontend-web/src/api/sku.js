import request, { unwrapList } from '@/utils/request'

/**
 * 查询 SKU 库存列表（后端一次性返回全部，无分页）
 * @returns {Promise<{ rows: Array, total: number }>}
 */
export function listSku() {
  return request({
    url: '/skus',
    method: 'get'
  }).then((res) => unwrapList(res.data))
}

/**
 * 库存调整（留痕）
 * @param {number} id SKU ID
 * @param {object} data { delta, reason }
 * @returns {Promise<{ totalStock: number, availableStock: number }>}
 */
export function adjustStock(id, data) {
  return request({
    url: '/skus/' + id + '/stock',
    method: 'post',
    data
  })
}
