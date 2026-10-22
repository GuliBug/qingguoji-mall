import { createRequire } from 'module'

// H5 已自带 puppeteer 24.10.0；若本地缺失则回退到同机 Web 端的同版本。
const require = createRequire(import.meta.url)
let puppeteer
try {
  puppeteer = require('puppeteer')
} catch {
  puppeteer = require('D:/lookList/qingguoji-mall/frontend-web/node_modules/puppeteer')
}

export async function launchH5() {
  const browser = await puppeteer.launch({
    headless: 'new',
    args: ['--no-sandbox', '--disable-gpu', '--disable-dev-shm-usage']
  })
  const page = await browser.newPage()
  await page.setViewport({ width: 390, height: 844, isMobile: true })
  return { browser, page }
}

export { puppeteer }
