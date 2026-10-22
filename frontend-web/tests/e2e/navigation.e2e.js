import { test, assert } from './lib/harness.js'
import { textOf, loginAndGoto, BASE_URL } from './lib/browser.js'

test('侧边栏渲染六个业务菜单', async ({ browser }) => {
  const { page, close } = await loginAndGoto(browser, '/index')
  try {
    await page.waitForSelector('.sidebar-container .el-menu')
    const menus = await page.$$eval('.sidebar-container .el-menu-item', (els) => els.map((el) => el.innerText.trim()))
    const expected = ['首页看板', '商品管理', '库存管理', '订单管理', '售后管理', '会员管理']
    for (const name of expected) {
      assert(menus.includes(name), `侧边栏缺少菜单「${name}」，实际为：${menus.join(',')}`)
    }
    assert(menus.length === expected.length, `菜单数量应为 ${expected.length}，实际为 ${menus.length}`)
  } finally {
    await close()
  }
})

test('点击侧边栏菜单可跳转路由', async ({ browser }) => {
  const { page, close } = await loginAndGoto(browser, '/index')
  try {
    await page.waitForSelector('.sidebar-container .el-menu-item')
    const items = await page.$$('.sidebar-container .el-menu-item')
    // 第 2 个菜单为「商品管理」
    await items[1].click()
    await page.waitForFunction(() => location.pathname === '/product', { timeout: 15000 })
    const text = await textOf(page, '.table-toolbar')
    assert(text.includes('商品列表'), `跳转后未渲染商品列表，实际为：${text}`)
  } finally {
    await close()
  }
})

test('商品管理页加载种子数据并用 SKU 聚合出价格区间', async ({ browser }) => {
  const { page, close } = await loginAndGoto(browser, '/product')
  try {
    await page.waitForSelector('.el-table__row')
    const rowCount = await page.$$eval('.el-table__body .el-table__row', (els) => els.length)
    assert(rowCount > 0, '商品列表为空，种子数据未生效')

    const tableText = await textOf(page, '.table-card')
    assert(tableText.includes('经典系列'), `商品列表未包含种子分类「经典系列」：${tableText.slice(0, 120)}`)
    assert(tableText.includes('¥'), '商品列表未渲染价格区间（应由 SKU 聚合得出）')

    const pager = await page.$eval('.pagination-container', (el) => el.innerText)
    assert(pager.includes('10'), `分页总数应为 10，实际为：${pager}`)
  } finally {
    await close()
  }
})

test('库存管理页可售库存 = 总库存 - 锁定库存', async ({ browser }) => {
  const { page, close } = await loginAndGoto(browser, '/stock')
  try {
    await page.waitForSelector('.el-table__row')
    // 等待商品名映射完成（SKU 接口只返回 productId）
    await page.waitForFunction(() => document.querySelector('.table-card').innerText.includes('槟榔'), {
      timeout: 15000
    })

    const rows = await page.$$eval('.el-table__body .el-table__row', (els) =>
      els.map((tr) => Array.from(tr.querySelectorAll('td')).map((td) => td.innerText.trim()))
    )
    assert(rows.length > 0, 'SKU 列表为空')

    // 列顺序：SKU ID / 所属商品 / 规格 / SKU编码 / 单价 / 总库存 / 锁定库存 / 可售库存 / 操作
    const row = rows[0]
    const totalStock = Number(row[5])
    const lockedStock = Number(row[6])
    const available = Number(row[7])
    assert(Number(row[0]) > 0, `SKU ID 异常：${row[0]}`)
    assert(available === totalStock - lockedStock, `可售库存计算错误：${totalStock} - ${lockedStock} != ${available}`)
  } finally {
    await close()
  }
})

test('订单 / 售后 / 会员页均可打开且表格渲染正常', async ({ browser }) => {
  const { page, close } = await loginAndGoto(browser, '/order')
  try {
    const pages = [
      { path: '/order', title: '订单列表' },
      { path: '/aftersale', title: '售后列表' },
      { path: '/member', title: '会员列表' }
    ]
    for (const item of pages) {
      await page.goto(BASE_URL + item.path, { waitUntil: 'networkidle2' })
      await page.waitForSelector('.table-card')
      const text = await textOf(page, '.table-toolbar')
      assert(text.includes(item.title), `${item.path} 未渲染「${item.title}」，实际为：${text}`)

      const headers = await page.$$eval('.el-table__header th', (els) => els.length)
      assert(headers > 0, `${item.path} 表格表头缺失`)
    }
  } finally {
    await close()
  }
})

test('退出登录后回到登录页且清除登录态', async ({ browser }) => {
  const { page, close } = await loginAndGoto(browser, '/index')
  try {
    await page.waitForSelector('.avatar-wrapper')
    await page.click('.avatar-wrapper')
    await page.waitForSelector('.el-dropdown-menu__item')

    // 下拉菜单通过 teleport 渲染，popper 为 absolute 定位（offsetParent 不可靠），
    // 因此用 getClientRects() 判断是否真实可见；先等菜单完成展开再点击
    await page.waitForFunction(
      () => [...document.querySelectorAll('.el-dropdown-menu__item')].some((el) => el.getClientRects().length > 0),
      { timeout: 10000 }
    )

    const clicked = await page.evaluate(() => {
      const items = [...document.querySelectorAll('.el-dropdown-menu__item')].filter(
        (el) => el.getClientRects().length > 0
      )
      if (items.length === 0) {
        return false
      }
      items[items.length - 1].click()
      return true
    })
    assert(clicked, '未找到可点击的下拉菜单项')

    await page.waitForSelector('.el-message-box')
    await page.click('.el-message-box__btns .el-button--primary')

    await page.waitForFunction(() => location.pathname === '/login', { timeout: 20000 })

    const cookies = await page.cookies()
    const tokenCookie = cookies.find((c) => c.name === 'Admin-Token')
    assert(!tokenCookie || !tokenCookie.value, '退出后 Admin-Token 未清除')
  } finally {
    await close()
  }
})
