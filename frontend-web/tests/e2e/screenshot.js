import { mkdirSync } from 'node:fs'
import { launchBrowser, closeBrowser, loginAndGoto, BASE_URL, ADMIN } from './lib/browser.js'

const OUT = process.env.SHOT_DIR || 'C:/Users/27978/WorkBuddy/2026-10-22-10-01-49/outputs'
mkdirSync(OUT, { recursive: true })

const browser = await launchBrowser()

try {
  // 1. 登录页
  const loginSession = await (async () => {
    const ctx = await browser.createBrowserContext()
    const page = await ctx.newPage()
    await page.goto(BASE_URL + '/login', { waitUntil: 'networkidle2' })
    await page.waitForSelector('.login-form')
    await new Promise((r) => setTimeout(r, 500))
    await page.screenshot({ path: OUT + '/01-login.png' })
    await page.click('.login-form .el-button--primary')
    await page.waitForFunction(() => location.pathname === '/index', { timeout: 20000 })
    return { page, close: () => ctx.close() }
  })()
  await new Promise((r) => setTimeout(r, 1500))
  await loginSession.page.screenshot({ path: OUT + '/02-dashboard.png' })
  await loginSession.close()

  // 2. 商品管理
  const p1 = await loginAndGoto(browser, '/product')
  await p1.page.waitForSelector('.el-table__row')
  await new Promise((r) => setTimeout(r, 1200))
  await p1.page.screenshot({ path: OUT + '/03-product.png' })
  await p1.close()

  // 3. 库存管理
  const p2 = await loginAndGoto(browser, '/stock')
  await p2.page.waitForSelector('.el-table__row')
  await new Promise((r) => setTimeout(r, 1200))
  await p2.page.screenshot({ path: OUT + '/04-stock.png' })
  await p2.close()

  console.log('screenshots saved to', OUT)
  console.log('admin account used:', ADMIN.username)
} finally {
  await closeBrowser(browser)
}
