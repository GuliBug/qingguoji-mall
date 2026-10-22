/**
 * 极简测试运行器：不额外引入测试框架，仅用 puppeteer + node:assert
 * 用例用 test(name, fn) 注册，runAll() 顺序执行并汇总结果
 */

const cases = []

/**
 * 注册用例
 * @param {string} name 用例名
 * @param {Function} fn 用例实现，入参为上下文对象
 */
export function test(name, fn) {
  cases.push({ name, fn })
}

/**
 * 断言失败时抛出统一格式错误
 * @param {boolean} condition
 * @param {string} message
 */
export function assert(condition, message) {
  if (!condition) {
    throw new Error(message || '断言失败')
  }
}

/**
 * 顺序执行全部用例
 * @param {object} context 传给用例的上下文（browser / page 等）
 * @returns {Promise<{ passed: number, failed: number, failures: Array }>}
 */
export async function runAll(context) {
  let passed = 0
  const failures = []

  for (const item of cases) {
    const start = Date.now()
    try {
      await item.fn(context)
      const cost = Date.now() - start
      console.log(`  \u2713 ${item.name} (${cost}ms)`)
      passed += 1
    } catch (error) {
      const cost = Date.now() - start
      console.log(`  \u2717 ${item.name} (${cost}ms)`)
      console.log(`      ${error && error.message ? error.message : error}`)
      failures.push({ name: item.name, error })
    }
  }

  return { passed, failed: failures.length, failures }
}
