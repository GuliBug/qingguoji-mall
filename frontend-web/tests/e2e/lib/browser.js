import puppeteer from 'puppeteer'

export const BASE_URL = process.env.E2E_BASE_URL || 'http://localhost:5173'
export const ADMIN = {
  username: process.env.E2E_USERNAME || 'admin',
  password: process.env.E2E_PASSWORD || 'admin123'
}

/**
 * 启动浏览器
 * @returns {Promise<import('puppeteer').Browser>}
 */
export async function launchBrowser() {
  return puppeteer.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-dev-shm-usage'],
    defaultViewport: { width: 1360, height: 900 }
  })
}

/**
 * 创建隔离的页面（独立 cookie / storage，避免用例之间互相污染登录态）
 * @param {import('puppeteer').Browser} browser
 * @returns {Promise<{ page: import('puppeteer').Page, close: Function }>}
 */
export async function newIsolatedPage(browser) {
  const context = await browser.createBrowserContext()
  const page = await context.newPage()
  return {
    page,
    /**
     * 关闭页面与上下文
     */
    async close() {
      await context.close()
    }
  }
}

/**
 * 设置输入框的值（走原生 input 事件，确保 Vue 的 v-model 同步）
 * @param {import('puppeteer').Page} page
 * @param {string} selector
 * @param {string} value
 */
export async function setInput(page, selector, value) {
  await page.waitForSelector(selector, { timeout: 15000 })
  await page.$eval(
    selector,
    (el, val) => {
      el.value = val
      el.dispatchEvent(new Event('input', { bubbles: true }))
      el.dispatchEvent(new Event('change', { bubbles: true }))
    },
    value
  )
}

/**
 * 等待元素出现并返回其文本
 * @param {import('puppeteer').Page} page
 * @param {string} selector
 * @param {number} timeout
 * @returns {Promise<string>}
 */
export async function textOf(page, selector, timeout = 15000) {
  await page.waitForSelector(selector, { timeout })
  return page.$eval(selector, (el) => el.textContent.trim())
}

/**
 * 判断页面上是否存在指定文案
 * @param {import('puppeteer').Page} page
 * @param {string} text
 * @returns {Promise<boolean>}
 */
export async function hasText(page, text) {
  return page.evaluate((t) => document.body.innerText.includes(t), text)
}

/**
 * 在登录页执行登录
 * @param {import('puppeteer').Page} page
 * @param {{ username: string, password: string }} account
 */
export async function doLogin(page, account = ADMIN) {
  await page.goto(BASE_URL + '/login', { waitUntil: 'networkidle2' })
  await page.waitForSelector('.login-form')

  await setInput(page, '.login-form input[type="text"]', account.username)
  await setInput(page, '.login-form input[type="password"]', account.password)

  await page.click('.login-form .el-button--primary')
}

/**
 * 登录并进入指定路由
 * @param {import('puppeteer').Browser} browser
 * @param {string} path
 * @returns {Promise<{ page: import('puppeteer').Page, close: Function }>}
 */
export async function loginAndGoto(browser, path) {
  const session = await newIsolatedPage(browser)
  await doLogin(session.page, ADMIN)
  await session.page.waitForFunction(() => location.pathname === '/index', { timeout: 20000 })
  if (path && path !== '/index') {
    await session.page.goto(BASE_URL + path, { waitUntil: 'networkidle2' })
  }
  return session
}

/**
 * 关闭浏览器
 * @param {import('puppeteer').Browser} browser
 */
export async function closeBrowser(browser) {
  if (browser) {
    await browser.close()
  }
}
