/**
 * 分页参数适配：若依习惯 pageNum/pageSize（从 1 开始）→ 后端 page/size（从 0 开始）
 * @param {object} query 查询条件
 * @returns {object} 传给后端的 params
 */
export function toPageParams(query = {}) {
  const { pageNum = 1, pageSize = 10, ...rest } = query
  return {
    ...rest,
    page: Math.max(0, Number(pageNum) - 1),
    size: Number(pageSize)
  }
}
