// H5（Taro H5）专属 E2E 助手。
// 关键约定（来自排查结论）：
// 1) Taro H5 底部 tabBar 是 .weui-tabbar__item 锚点，必须用真实点击触发路由，
//    不能改 location.hash，否则 useDidShow(load) 不会触发。
// 2) Taro 弹窗 .taro-model__confirm 通过原生 onclick 绑定，需调用 onclick() 触发。

export async function tap(page, handle) {
  await handle.click()
}

export async function waitSel(page, selector, timeout = 10000) {
  await page.waitForSelector(selector, { timeout })
}

// 点击底部 tab（按 .weui-tabbar__label 文本匹配）
export async function gotoTab(page, label) {
  await page.waitForSelector('.weui-tabbar__item', { timeout: 10000 })
  const ok = await page.evaluate((text) => {
    const items = [...document.querySelectorAll('.weui-tabbar__item')]
    const it = items.find((a) => {
      const l = a.querySelector('.weui-tabbar__label')
      return l && l.textContent?.trim() === text
    })
    if (it) {
      it.click()
      return true
    }
    return false
  }, label)
  if (!ok) throw new Error('未找到 tab: ' + label)
  await new Promise((r) => setTimeout(r, 1200))
}

// 关闭 Taro 弹窗，点确认按钮
export async function handleModal(page) {
  await page.waitForSelector('.taro-model__confirm', { timeout: 6000 })
  await page.evaluate(() => {
    const b = document.querySelector('.taro-model__confirm')
    if (b && typeof b.onclick === 'function') b.onclick()
    else if (b) b.click()
  })
  await new Promise((r) => setTimeout(r, 900))
}
