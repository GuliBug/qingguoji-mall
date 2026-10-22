import { launchBrowser, closeBrowser, BASE_URL } from './lib/browser.js'
import { runAll } from './lib/harness.js'
import { assert } from './lib/harness.js'

// 注册用例（导入即注册）
import './login.e2e.js'
import './navigation.e2e.js'

/**
 * 检查前端服务是否可访问
 * @returns {Promise<boolean>}
 */
async function checkServer() {
  try {
    const res = await fetch(BASE_URL, { method: 'GET' })
    return res.ok
  } catch {
    return false
  }
}

async function main() {
  console.log('\n=== 青果纪 Web 管理端 E2E 测试 ===')
  console.log('目标地址：' + BASE_URL)

  const serverAlive = await checkServer()
  if (!serverAlive) {
    console.error('\n[ERROR] 前端服务未启动，请先执行 npm run dev')
    process.exit(1)
  }

  const browser = await launchBrowser()
  let result = { passed: 0, failed: 0, failures: [] }
  try {
    result = await runAll({ browser, baseUrl: BASE_URL, assert })
  } finally {
    await closeBrowser(browser)
  }

  console.log('\n=== 结果 ===')
  console.log(`通过：${result.passed}    失败：${result.failed}`)
  if (result.failed > 0) {
    console.log('\n失败明细：')
    for (const item of result.failures) {
      console.log(`  - ${item.name}: ${item.error && item.error.message ? item.error.message : item.error}`)
    }
    process.exit(1)
  }
  console.log('全部通过 \n')
}

main().catch((error) => {
  console.error('[FATAL]', error)
  process.exit(1)
})
