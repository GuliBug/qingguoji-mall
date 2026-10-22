// 轻量测试记录器：逐步执行、收集通过/失败，并返回结构化结果供汇总。
export function createRecorder() {
  const results = []
  const step = async (name, fn) => {
    try {
      const detail = await fn()
      results.push({ name, ok: true, detail: detail ?? '' })
      console.log('  OK   ' + name + (detail ? ' -> ' + detail : ''))
      return detail
    } catch (e) {
      results.push({ name, ok: false, detail: e.message })
      console.log('  FAIL ' + name + ' -> ' + e.message)
      return null
    }
  }
  return { results, step }
}

export function assert(cond, msg) {
  if (!cond) throw new Error(msg)
}
