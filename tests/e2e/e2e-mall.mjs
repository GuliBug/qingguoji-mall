/**
 * E2E 端到端测试：青果纪商城全需求链路
 * 对象：运行中的后端 http://localhost:8080（真实 HTTP，覆盖 H5 客户端 + 管理端全部接口）
 * 运行：node tests/e2e/e2e-mall.mjs
 */
const B = 'http://localhost:8080'
const H5 = 'http://localhost:10086'
const results = []
const called = new Set() // 本次真实调用的接口（归一化），用于 H5 api.ts 对账
let buyerToken = '', adminToken = ''

function normPath(p) {
  return p.replace(/\?.*$/, '')
    .replace(/\/\$\{[^}]+\}/g, '/{v}')
    .replace(/\/[A-Z][A-Z0-9\-]{6,}/g, '/{v}') // 订单号/售后单号
    .replace(/\/\d+/g, '/{v}')                  // 数字 id
}

async function req(method, path, body, token) {
  called.add(normPath(path))
  const r = await fetch(B + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { 'X-Token': token } : {}) },
    body: body ? JSON.stringify(body) : undefined
  })
  return r.json()
}

function check(id, name, cond, detail) {
  results.push({ id, name, pass: !!cond, detail: detail || '' })
  console.log(`${cond ? '✅ PASS' : '❌ FAIL'} [${id}] ${name}${detail ? ' — ' + detail : ''}`)
}

;(async () => {
  const phone = '139' + String(Date.now()).slice(-8) // 每次运行新会员，避免历史数据干扰

  // ===== 一、会员模块 =====
  const badLogin = await req('POST', '/api/member/login', { phone, smsCode: '000000' })
  check('E2E-01', '错误验证码被拒绝', badLogin.code !== 200, badLogin.msg)

  const login = await req('POST', '/api/member/login', { phone, smsCode: '123456' })
  buyerToken = login.data?.token
  check('E2E-02', '新会员登录注册（返回token）', login.code === 200 && !!buyerToken, `等级 ${login.data?.levelName}`)

  const ck = await req('POST', '/api/member/checkin', null, buyerToken)
  check('E2E-03', '每日签到 +5 积分', ck.code === 200 && ck.data?.points === 5)
  const ck2 = await req('POST', '/api/member/checkin', null, buyerToken)
  check('E2E-04', '重复签到被拦截', ck2.code !== 200, ck2.msg)
  const scanBad = await req('POST', '/api/member/scan', { code: 'NOT-EXIST-CODE' }, buyerToken)
  check('E2E-05', '无效防伪码被拒（防重分支）', scanBad.code !== 200, scanBad.msg)
  const logs = await req('GET', '/api/member/points/logs', null, buyerToken)
  check('E2E-06', '积分流水留痕（CHECKIN）', logs.code === 200 && logs.data?.content?.some(l => l.type === 'CHECKIN'))

  // ===== 二、商品模块 =====
  const list = await req('GET', '/api/products?size=20')
  check('E2E-07', '商品列表（销量倒序分页）', list.code === 200 && list.data?.totalElements >= 10, `共 ${list.data?.totalElements} 款`)
  const sorted = [...(list.data?.content || [])]
  check('E2E-08', '列表按销量倒序', sorted.every((p, i) => i === 0 || sorted[i - 1].sales >= p.sales))
  const pid = list.data.content[0].id
  const det = await req('GET', `/api/products/${pid}`)
  const sku = det.data?.skus?.[0]
  check('E2E-09', '商品详情含SKU与可售库存', det.code === 200 && sku && sku.totalStock - sku.lockedStock > 0,
    `${det.data?.name} 可售 ${sku?.totalStock - sku?.lockedStock}`)

  // ===== 三、购物车模块 =====
  const add = await req('POST', '/api/cart', { skuId: sku.id, qty: 3 }, buyerToken)
  check('E2E-10', '加入购物车', add.code === 200)
  const add2 = await req('POST', '/api/cart', { skuId: sku.id, qty: 1 }, buyerToken)
  const cart = await req('GET', '/api/cart', null, buyerToken)
  const line = cart.data?.find(c => c.skuId === sku.id)
  check('E2E-11', '同SKU加购合并数量（3+1=4）', add2.code === 200 && line?.qty === 4)
  const upd = await req('PUT', `/api/cart/${line.cartId}`, { qty: 2 }, buyerToken)
  const cart2 = await req('GET', '/api/cart', null, buyerToken)
  check('E2E-12', '修改购物车数量（4→2）', upd.code === 200 && cart2.data.find(c => c.cartId === line.cartId)?.qty === 2)

  // ===== 四、订单模块（下单→支付→发货→收货）=====
  const price = Number(sku.price)
  const order = await req('POST', '/api/orders', {
    lines: [{ skuId: sku.id, qty: 2 }],
    receiver: 'E2E测试', receiverPhone: phone, address: '长沙市测试路1号'
  }, buyerToken)
  const expectPay = price * 2 < 99 ? price * 2 + 8 : price * 2
  check('E2E-13', '下单成功且金额正确（折扣/运费）', order.code === 200 && Number(order.data.payAmount) === expectPay,
    `实付 ¥${order.data?.payAmount}（预期 ¥${expectPay}，${price * 2 < 99 ? '含运费8' : '免运费'}）`)
  check('E2E-14', '15分钟支付期限已设定', !!order.data?.payDeadline)
  const orderNo = order.data.orderNo

  const pay = await req('POST', `/api/orders/${orderNo}/pay`, {}, buyerToken)
  check('E2E-15', '支付成功（PAID）+ 发放积分', pay.code === 200 && pay.data.status === 'PAID' && pay.data.pointsEarned > 0,
    `积分 +${pay.data?.pointsEarned}`)
  const pay2 = await req('POST', `/api/orders/${orderNo}/pay`, {}, buyerToken)
  check('E2E-16', '重复支付幂等拦截', pay2.code !== 200, pay2.msg)

  const cancelO = await req('POST', '/api/orders', {
    lines: [{ skuId: sku.id, qty: 1 }], receiver: 'E2E', receiverPhone: phone, address: '长沙'
  }, buyerToken)
  const cancel = await req('POST', `/api/orders/${cancelO.data.orderNo}/cancel`, {}, buyerToken)
  const list2 = await req('GET', '/api/orders?status=CANCELED', null, buyerToken)
  check('E2E-17', '取消订单并可在已取消Tab查询', cancel.code === 200 && list2.data?.content?.some(o => o.orderNo === cancelO.data.orderNo))

  // ===== 五、管理端模块 =====
  const aLogin = await req('POST', '/admin-api/login', { username: 'admin', password: 'admin123' })
  adminToken = aLogin.data?.token
  check('E2E-18', '管理端独立登录', aLogin.code === 200 && !!adminToken)
  const dash = await req('GET', '/admin-api/dashboard', null, adminToken)
  check('E2E-19', '数据看板统计', dash.code === 200 && dash.data != null)
  const ship = await req('POST', `/admin-api/orders/${orderNo}/ship`, { logisticsNo: 'SF-E2E-001' }, adminToken)
  check('E2E-20', '管理端发货（记物流单号）', ship.code === 200)
  const confirm = await req('POST', `/api/orders/${orderNo}/confirm`, {}, buyerToken)
  check('E2E-21', '买家确认收货（COMPLETED）', confirm.code === 200)

  // ===== 六、售后模块（仅退款链路：须在已支付未发货订单上申请）=====
  const refundOrder = await req('POST', '/api/orders', {
    lines: [{ skuId: sku.id, qty: 1 }], receiver: 'E2E', receiverPhone: phone, address: '长沙'
  }, buyerToken)
  const payRefund = await req('POST', `/api/orders/${refundOrder.data.orderNo}/pay`, {}, buyerToken)
  const myOrders = await req('GET', '/api/orders?size=5', null, buyerToken)
  const paidOrder = myOrders.data.content.find(o => o.orderNo === refundOrder.data.orderNo)
  const itemId = paidOrder.items[0].id
  const as = await req('POST', '/api/aftersale', { orderItemId: itemId, type: 'REFUND_ONLY', qty: 1, reason: 'E2E七天无理由', description: '' }, buyerToken)
  check('E2E-22', '申请仅退款（金额=单价）', as.code === 200 && Number(as.data?.refundAmount) === price, `退款 ¥${as.data?.refundAmount}`)
  const audit = await req('POST', `/admin-api/aftersales/${as.data.id}/audit`, { pass: true }, adminToken)
  check('E2E-23', '管理端审核通过（DONE）', audit.code === 200 && audit.data.status === 'DONE')
  const me = await req('GET', '/api/member/me', null, buyerToken)
  const back = Math.round(payRefund.data.pointsEarned * Number(as.data.refundAmount) / Number(refundOrder.data.payAmount))
  check('E2E-24', '退款后积分按比例扣回',
    me.data.member.points === pay.data.pointsEarned + payRefund.data.pointsEarned + 5 - back,
    `积分 ${me.data.member.points} = 订单1消费${pay.data.pointsEarned} + 订单2消费${payRefund.data.pointsEarned} + 签到5 - 扣回${back}`)
  const myAs = await req('GET', '/api/aftersale/my', null, buyerToken)
  check('E2E-25', '售后单可查询', myAs.code === 200 && myAs.data?.content?.some(a => a.id === as.data.id))

  // ===== 七、字段级校验（每个字段的空值/非法值/边界）=====
  const f26 = await req('POST', '/api/member/login', { phone: '123', smsCode: '123456' })
  check('E2E-26', '登录 phone 格式非法被拒', f26.code !== 200 && f26.msg.includes('手机号格式'), f26.msg)
  const f27 = await req('POST', '/api/cart', { skuId: sku.id, qty: 0 }, buyerToken)
  check('E2E-27', '加购 qty=0 被拒', f27.code !== 200 && f27.msg.includes('大于 0'), f27.msg)
  const f28 = await req('POST', '/api/orders', { lines: [{ skuId: sku.id, qty: 1 }], receiver: '', receiverPhone: phone, address: '长沙' }, buyerToken)
  check('E2E-28', '下单 receiver 空被拒', f28.code !== 200 && f28.msg.includes('收货人'), f28.msg)
  const f29 = await req('POST', '/api/orders', { lines: [{ skuId: sku.id, qty: 1 }], receiver: '张三', receiverPhone: phone, address: '' }, buyerToken)
  check('E2E-29', '下单 address 空被拒', f29.code !== 200 && f29.msg.includes('收货地址'), f29.msg)
  const f30 = await req('POST', '/api/member/scan', { code: '' }, buyerToken)
  check('E2E-30', '扫码 code 空被拒', f30.code !== 200 && f30.msg.includes('不能为空'), f30.msg)
  const f31 = await req('POST', '/api/aftersale', { orderItemId: itemId, type: 'EXCHANGE', qty: 1, reason: 'x', description: '' }, buyerToken)
  check('E2E-31', '售后 type 非法被拒', f31.code !== 200 && f31.msg.includes('售后类型不合法'), f31.msg)
  const f32 = await req('POST', `/admin-api/orders/${cancelO.data.orderNo}/ship`, { logisticsNo: '' }, adminToken)
  check('E2E-32', '管理端发货 logisticsNo 空被拒', f32.code !== 200 && f32.msg.includes('物流单号'), f32.msg)
  const skus = await req('GET', '/admin-api/skus', null, adminToken)
  const firstSkuId = skus.data[0].id
  const f33 = await req('POST', `/admin-api/skus/${firstSkuId}/stock`, { delta: 0 }, adminToken)
  check('E2E-33', '库存调整 delta=0 被拒', f33.code !== 200 && f33.msg.includes('不能为 0'), f33.msg)
  const members = await req('GET', '/admin-api/members', null, adminToken)
  const f34 = await req('POST', `/admin-api/members/${members.data.content[0].id}/points`, { delta: 0 }, adminToken)
  check('E2E-34', '积分调整 delta=0 被拒', f34.code !== 200 && f34.msg.includes('不能为 0'), f34.msg)
  const f35 = await req('POST', `/admin-api/products/${pid}/status`, { status: 2 }, adminToken)
  check('E2E-35', '商品状态值 2 非法被拒', f35.code !== 200 && f35.msg.includes('状态值不合法'), f35.msg)

  // ===== 八、退货退款全链路（RETURN_REFUND：申请→审核→寄回→确认收货→库存回补+积分扣回）=====
  const rr = await req('POST', '/api/orders', {
    lines: [{ skuId: sku.id, qty: 1 }], receiver: 'E2E退货', receiverPhone: phone, address: '长沙市退货路2号'
  }, buyerToken)
  const rrPay = await req('POST', `/api/orders/${rr.data.orderNo}/pay`, {}, buyerToken)
  const rrList = await req('GET', '/api/orders?size=10', null, buyerToken)
  const rrItem = rrList.data.content.find(o => o.orderNo === rr.data.orderNo).items[0].id
  const skBefore = (await req('GET', `/api/products/${pid}`)).data.skus.find(s => s.id === sku.id)
  const saleBefore = skBefore.totalStock - skBefore.lockedStock

  const ras = await req('POST', '/api/aftersale', { orderItemId: rrItem, type: 'RETURN_REFUND', qty: 1, reason: 'E2E退货退款', description: '' }, buyerToken)
  check('E2E-36', '申请退货退款（WAIT_AUDIT）', ras.code === 200 && ras.data.status === 'WAIT_AUDIT', `退款 ¥${ras.data?.refundAmount}`)
  const rEarly = await req('POST', `/admin-api/aftersales/${ras.data.id}/confirm-return`, {}, adminToken)
  check('E2E-37', '未寄回时确认收货被拦截', rEarly.code !== 200, rEarly.msg)
  const raudit = await req('POST', `/admin-api/aftersales/${ras.data.id}/audit`, { pass: true }, adminToken)
  check('E2E-38', '退货退款审核通过（WAIT_RETURN）', raudit.code === 200 && raudit.data.status === 'WAIT_RETURN')
  const badLogi = await req('POST', `/api/aftersale/${ras.data.id}/return-logistics`, { logisticsNo: '' }, buyerToken)
  check('E2E-39', '退货物流单号为空被拒', badLogi.code !== 200, badLogi.msg)
  const rlogi = await req('POST', `/api/aftersale/${ras.data.id}/return-logistics`, { logisticsNo: 'YT-E2E-002' }, buyerToken)
  const rlogiCheck = rlogi.code === 200 ? (await req('GET', '/api/aftersale/my', null, buyerToken)).data.content.find(a => a.id === ras.data.id) : null
  check('E2E-40', '买家填写退货物流（WAIT_RECEIVE）', rlogi.code === 200 && rlogiCheck?.status === 'WAIT_RECEIVE')
  const ptsBeforeDone = (await req('GET', '/api/member/me', null, buyerToken)).data.member.points
  const rdone = await req('POST', `/admin-api/aftersales/${ras.data.id}/confirm-return`, {}, adminToken)
  check('E2E-41', '商家确认收货完成退款（DONE）', rdone.code === 200 && rdone.data.status === 'DONE')
  const skAfter = (await req('GET', `/api/products/${pid}`)).data.skus.find(s => s.id === sku.id)
  check('E2E-42', '退货回补库存（可售 +1）', skAfter.totalStock - skAfter.lockedStock === saleBefore + 1,
    `${saleBefore} → ${skAfter.totalStock - skAfter.lockedStock}`)
  const meAfterRR = (await req('GET', '/api/member/me', null, buyerToken)).data.member.points
  const back2 = Math.round(rrPay.data.pointsEarned * Number(ras.data.refundAmount) / Number(rr.data.payAmount))
  check('E2E-43', '退货退款积分按比例扣回', meAfterRR === ptsBeforeDone - back2,
    `${ptsBeforeDone} → ${meAfterRR}（扣回 ${back2}）`)

  // ===== 九、Web 管理端与 H5 客户端联调覆盖 =====
  const adminPage = await fetch(B + '/admin.html').then(r => r.status === 200 ? r.text() : '').catch(() => '')
  check('E2E-44', 'Web 管理端页面可达且模块齐全（若依风格）',
    adminPage.includes('商品管理') && adminPage.includes('售后管理') && adminPage.includes('会员管理') && adminPage.includes('/admin-api'))
  const [h5idx, h5prod, h5cart, h5order, h5member] = await Promise.all(
    ['index', 'product', 'cart', 'orders', 'member'].map(p =>
      fetch(`${H5}/pages/${p}/index.vue`).then(r => r.status).catch(() => 0)))
  check('E2E-45', 'H5 五个页面模块联调编译正常', [h5idx, h5prod, h5cart, h5order, h5member].every(s => s === 200),
    `status: ${[h5idx, h5prod, h5cart, h5order, h5member].join('/')}`)
  const h5apiSrc = await import('node:fs').then(fs =>
    fs.readFileSync(new URL('../../frontend-h5/mall-client/src/api.ts', import.meta.url), 'utf-8'))
  const h5Paths = new Set([...h5apiSrc.matchAll(/request\('(?:GET|POST|PUT)',\s*(?:`([^`]+)`|'([^']+)')/g)]
    .map(m => normPath(m[1] || m[2])))
  const missing = [...h5Paths].filter(p => !called.has(p))
  check('E2E-46', 'H5 api.ts 全部接口均被本次 E2E 真实执行（契约对账）',
    h5Paths.size >= 15 && missing.length === 0,
    missing.length ? `未覆盖: ${missing.join(', ')}` : `共 ${h5Paths.size} 个接口对账一致`)

  // ===== 汇总 =====
  const pass = results.filter(r => r.pass).length
  console.log(`\n========== E2E 结果：${pass}/${results.length} 通过 ==========`)
  console.log(JSON.stringify(results))
  process.exit(pass === results.length ? 0 : 1)
})().catch(e => { console.error('E2E 执行异常:', e.message); process.exit(1) })
