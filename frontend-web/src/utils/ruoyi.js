/**
 * 通用工具方法（对齐若依 utils/ruoyi.js 的常用部分）
 */

/**
 * 日期格式化
 * @param {string|number|Date} time
 * @param {string} pattern 例如 '{y}-{m}-{d} {h}:{i}:{s}'
 * @returns {string}
 */
export function parseTime(time, pattern) {
  if (!time || arguments.length === 0) {
    return null
  }
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if (typeof time === 'string' && /^[0-9]+$/.test(time)) {
      time = parseInt(time, 10)
    } else if (typeof time === 'string') {
      time = time
        .replace(/-/g, '/')
        .replace('T', ' ')
        .replace(/\.\d+$/, '')
    }
    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000
    }
    date = new Date(time)
  }
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  return format.replace(/{([ymdhisa])+}/g, (result, key) => {
    let value = formatObj[key]
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
}

/**
 * 对象转 URL 参数
 * @param {object} params
 * @returns {string}
 */
export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName]
    const part = encodeURIComponent(propName) + '='
    if (value !== null && value !== '' && typeof value !== 'undefined') {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== '' && typeof value[key] !== 'undefined') {
            const params = propName + '[' + key + ']'
            const subPart = encodeURIComponent(params) + '='
            result += subPart + encodeURIComponent(value[key]) + '&'
          }
        }
      } else {
        result += part + encodeURIComponent(value) + '&'
      }
    }
  }
  return result
}

/**
 * 添加日期范围参数（若依表格搜索常用）
 * @param {object} params
 * @param {Array} dateRange
 * @param {string} propName
 * @returns {object}
 */
export function addDateRange(params, dateRange, propName) {
  const search = params
  search.params =
    typeof search.params === 'object' && search.params !== null && !Array.isArray(search.params) ? search.params : {}
  dateRange = Array.isArray(dateRange) ? dateRange : []
  if (typeof propName === 'undefined' || propName === null) {
    search.params['beginTime'] = dateRange[0]
    search.params['endTime'] = dateRange[1]
  } else {
    search.params['begin' + propName] = dateRange[0]
    search.params['end' + propName] = dateRange[1]
  }
  return search
}

/**
 * 从字典数组中取标签
 * @param {Array} datas 字典数组
 * @param {*} value 值
 * @param {string} defaultLabel 默认展示
 * @returns {string}
 */
export function selectDictLabel(datas, value, defaultLabel) {
  if (!Array.isArray(datas) || value === undefined) {
    return defaultLabel || ''
  }
  const target = datas.find((item) => String(item.value) === String(value))
  return target ? target.label : defaultLabel || String(value)
}

/**
 * 构造树形数据（若依 handleTree 精简版）
 * @param {Array} data
 * @param {string} id
 * @param {string} parentId
 * @param {string} children
 * @returns {Array}
 */
export function handleTree(data, id = 'id', parentId = 'parentId', children = 'children') {
  const childrenListMap = {}
  const nodeIds = {}
  const tree = []
  for (const d of data) {
    const pid = d[parentId]
    if (childrenListMap[pid] == null) {
      childrenListMap[pid] = []
    }
    nodeIds[d[id]] = d
    childrenListMap[pid].push(d)
  }
  for (const d of data) {
    const pid = d[parentId]
    if (nodeIds[pid] == null) {
      tree.push(d)
    }
  }
  const adaptToChildrenList = (o) => {
    if (childrenListMap[o[id]] !== null) {
      o[children] = childrenListMap[o[id]]
    }
    if (o[children]) {
      for (const c of o[children]) {
        adaptToChildrenList(c)
      }
    }
  }
  for (const t of tree) {
    adaptToChildrenList(t)
  }
  return tree
}
