/**
 * 业务状态字典
 * 后端以字符串/数字编码返回，前端统一由此映射为标签与颜色
 */

// 订单状态
export const ORDER_STATUS = [
  { value: 'WAIT_PAY', label: '待付款', type: 'warning' },
  { value: 'PAID', label: '已付款', type: 'primary' },
  { value: 'SHIPPED', label: '已发货', type: 'primary' },
  { value: 'COMPLETED', label: '已完成', type: 'success' },
  { value: 'CANCELED', label: '已取消', type: 'info' },
  { value: 'CLOSED', label: '已关闭', type: 'info' }
]

// 售后状态
export const AFTERSALE_STATUS = [
  { value: 'WAIT_AUDIT', label: '待审核', type: 'warning' },
  { value: 'WAIT_RETURN', label: '待寄回', type: 'primary' },
  { value: 'WAIT_RECEIVE', label: '待收货', type: 'primary' },
  { value: 'DONE', label: '已完成', type: 'success' },
  { value: 'REJECTED', label: '已拒绝', type: 'info' }
]

// 售后类型
export const AFTERSALE_TYPE = [
  { value: 'REFUND_ONLY', label: '仅退款', type: 'primary' },
  { value: 'RETURN_REFUND', label: '退货退款', type: 'warning' }
]

// 商品状态
export const PRODUCT_STATUS = [
  { value: 1, label: '在售', type: 'success' },
  { value: 0, label: '已下架', type: 'info' }
]

// 会员等级（后端 level 为 0~4 的整数）
export const MEMBER_LEVEL = [
  { value: 0, label: '注册会员', type: 'info' },
  { value: 1, label: '铜卡', type: 'info' },
  { value: 2, label: '银卡', type: 'primary' },
  { value: 3, label: '金卡', type: 'warning' },
  { value: 4, label: '黑金卡', type: 'danger' }
]

// 积分流水类型
export const POINTS_TYPE = [
  { value: 'EARN', label: '消费获得', type: 'success' },
  { value: 'REFUND_BACK', label: '退款回退', type: 'warning' },
  { value: 'DEDUCT', label: '扣减', type: 'danger' },
  { value: 'EXCHANGE', label: '兑换消耗', type: 'danger' }
]

/**
 * 根据字典查标签
 * @param {Array} dict 字典数组
 * @param {*} value 值
 * @returns {{ label: string, type: string }}
 */
export function dictLabel(dict, value) {
  const hit = dict.find((item) => String(item.value) === String(value))
  return hit ? { label: hit.label, type: hit.type } : { label: String(value ?? '—'), type: 'info' }
}
