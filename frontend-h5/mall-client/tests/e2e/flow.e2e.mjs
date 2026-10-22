import { launchH5 } from './lib/browser.mjs'
import { createRecorder } from './lib/harness.mjs'
import { tap, waitSel, gotoTab, handleModal } from './lib/h5.mjs'
import { mkdirSync } from 'fs'

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
    await rec.step('首页商品', async () => {
      await waitSel(page, '.goods-card', 15000)
      return (await page.$$('.goods-card')).length + ' 个'
    })
    await rec.step('点击一键登录', async () => {
      const b = await page.$('.login-btn')
      if (!b) throw new Error('无一键登录按钮')
      await tap(page, b)
      await handleModal(page)
      return 'ok'
    })
    await rec.step('登录态生效', async () => {
      await page.waitForFunction(() => document.querySelector('.member-info'), { timeout: 8000 })
      return await page.$eval('.member-info', (e) => e.textContent?.trim())
    })
    await rec.step('进详情', async () => {
      const cards = await page.$$('.goods-card')
      await tap(page, cards[0])
      await page.waitForFunction(() => location.hash.includes('product'), { timeout: 15000 })
      return page.url()
    })
    await rec.step('详情SKU', async () => {
      await waitSel(page, '.chip', 10000)
      return (await page.$$('.chip')).length + ' 个SKU'
    })
    await rec.step('加购', async () => {
      const b = await page.$('.btn-cart')
      if (!b) throw new Error('无加入购物车按钮')
      await tap(page, b)
      await new Promise((r) => setTimeout(r, 1500))
      return 'ok'
    })
    await rec.step('去购物车', async () => {
      await gotoTab(page, '购物车')
      await waitSel(page, '.cart-item, .empty', 10000)
      return 'ok'
    })
    await rec.step('购物车列表', async () => {
      const n = (await page.$$('.cart-item')).length
      if (n === 0) throw new Error('购物车为空，加购未生效')
      return n + ' 项'
    })
    await rec.step('提交订单', async () => {
      const b = await page.$('.btn-red')
      if (!b) throw new Error('无提交订单按钮')
      await tap(page, b)
      await handleModal(page)
      return 'ok'
    })
    await rec.step('支付弹窗', async () => {
      await handleModal(page)
      return 'ok'
    })
    await rec.step('去订单页', async () => {
      await gotoTab(page, '订单')
      await waitSel(page, '.order-card, .empty', 10000)
      return 'ok'
    })
    await rec.step('订单出现', async () => {
      const n = (await page.$$('.order-card')).length
      if (n === 0) throw new Error('订单列表为空，下单/支付未生效')
      return n + ' 个订单'
    })
    await rec.step('截图闭环', async () => {
      mkdirSync('tests/e2e/screenshots', { recursive: true })
      await page.screenshot({ path: 'tests/e2e/screenshots/orders.png' })
      return 'saved'
    })
  } finally {
    await browser.close()
  }
  return { name: 'H5 购物闭环', results: rec.results, failedRequests: failed }
}

if (import.meta.url === `file://${process.argv[1]}`) {
  const r = await run()
  const failed = r.results.filter((x) => !x.ok).length
  process.exit(failed ? 1 : 0)
}
