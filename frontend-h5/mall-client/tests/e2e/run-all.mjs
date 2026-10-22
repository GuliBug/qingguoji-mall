import { run as runSmoke } from './smoke.e2e.mjs'
import { run as runFlow } from './flow.e2e.mjs'

const suites = [runSmoke, runFlow]
let allOk = true

for (const run of suites) {
  try {
    const r = await run()
    const passed = r.results.filter((x) => x.ok).length
    const failed = r.results.filter((x) => !x.ok).length
    console.log(`\n===== ${r.name}: ${passed} 通过 / ${failed} 失败 =====`)
    if (failed) allOk = false
    if (r.failedRequests && r.failedRequests.length) {
      console.log('  失败请求(>=400):')
      r.failedRequests.slice(0, 10).forEach((x) => console.log('    ' + x))
    }
  } catch (e) {
    console.log(`\n===== 套件执行异常: ${e.message} =====`)
    allOk = false
  }
}

process.exit(allOk ? 0 : 1)
