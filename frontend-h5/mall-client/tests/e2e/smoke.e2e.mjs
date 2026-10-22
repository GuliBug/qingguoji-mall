import { launchH5 } from './lib/browser.mjs'
import { createRecorder } from './lib/harness.mjs'
import { waitSel } from './lib/h5.mjs'

const BASE = process.env.H5_BASE || 'http://127.0.0.1:10086/'

export async function run() {
  const { browser, page } = await launchH5()
  const rec = createRecorder()
  const failed = []
  page.on('response', (r) => {
    if (r.status() >= 400 && !r.url().includes('favicon')) failed.push(`[${r.status()}] ${r.url()}`)
  })
  try {
    await rec.step('打开首页', async () => {
      await page.goto(BASE, { waitUntil: 'networkidle2', timeout: 30000 })
      return page.url()
    })
    await rec.step('底部 tabBar 渲染', async () => {
      await waitSel(page, '.weui-tabbar__item', 10000)
      const labels = await page.$$eval('.weui-tabbar__label', (els) => els.map((e) => e.textContent?.trim()))
      if (!labels.includes('首页')) throw new Error('tabBar 缺少首页')
      return labels.join('/')
    })
    await rec.step('商品列表加载', async () => {
      await waitSel(page, '.goods-card', 15000)
      return (await page.$$('.goods-card')).length + ' 个商品'
    })
  } finally {
    await browser.close()
  }
  return { name: 'H5 冒烟测试', results: rec.results, failedRequests: failed }
}

// 直接执行时运行本套件
if (import.meta.url === `file://${process.argv[1]}`) {
  const r = await run()
  const failed = r.results.filter((x) => !x.ok).length
  process.exit(failed ? 1 : 0)
}
