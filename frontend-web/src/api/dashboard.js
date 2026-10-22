import request from '@/utils/request'

/**
 * 查询数据看板
 * 后端返回：{ todaySales, waitShipOrders, waitAuditAfterSales, memberCount, lowStockSkuCount }
 * @returns {Promise<object>}
 */
export function getDashboard() {
  return request({
    url: '/dashboard',
    method: 'get'
  })
}
