import { test, assert } from './lib/harness.js'
import { doLogin, textOf, hasText, newIsolatedPage, loginAndGoto, BASE_URL, ADMIN } from './lib/browser.js'

test('登录页正常渲染，展示标题与默认账号提示', async ({ browser }) => {
  const { page, close } = await newIsolatedPage(browser)
  try {
    await page.goto(BASE_URL + '/login', { waitUntil: 'networkidle2' })
    await page.waitForSelector('.login-form')

    const title = await textOf(page, '.login-form .title')
    assert(title.includes('青果纪商城管理系统'), `标题不符，实际为：${title}`)

    const hasTip = await hasText(page, 'admin / admin123')
    assert(hasTip, '登录页未展示默认账号提示')
  } finally {
    await close()
  }
})

test('账号或密码错误时给出错误提示，且停留在登录页', async ({ browser }) => {
  const { page, close } = await newIsolatedPage(browser)
  try {
    await doLogin(page, { username: ADMIN.username, password: 'wrong-password' })

    await page.waitForSelector('.el-message--error', { timeout: 15000 })
    const msg = await textOf(page, '.el-message--error .el-message__content')
    assert(msg.includes('用户名或密码错误'), `错误提示不符，实际为：${msg}`)

    assert(page.url().includes('/login'), `登录失败后不应跳转，当前地址：${page.url()}`)
  } finally {
    await close()
  }
})

test('登录成功后跳转到看板并渲染六个统计卡片', async ({ browser }) => {
  const { page, close } = await loginAndGoto(browser, '/index')
  try {
    await page.waitForSelector('.stat-card')

    const cards = await page.$$eval('.stat-card', (els) => els.map((el) => el.innerText.trim()))
    assert(cards.length === 6, `看板卡片数量应为 6，实际为 ${cards.length}`)

    const joined = cards.join(' | ')
    assert(joined.includes('今日销售额'), `看板缺少「今日销售额」卡片：${joined}`)
    assert(joined.includes('待发货订单'), `看板缺少「待发货订单」卡片：${joined}`)
    assert(joined.includes('会员总数'), `看板缺少「会员总数」卡片：${joined}`)
  } finally {
    await close()
  }
})

test('未登录访问受保护路由会被重定向到登录页', async ({ browser }) => {
  const { page, close } = await newIsolatedPage(browser)
  try {
    await page.goto(BASE_URL + '/product', { waitUntil: 'networkidle2' })
    await page.waitForFunction(() => location.pathname === '/login', { timeout: 15000 })
    assert(page.url().includes('/login'), `应重定向到登录页，当前地址：${page.url()}`)
  } finally {
    await close()
  }
})
