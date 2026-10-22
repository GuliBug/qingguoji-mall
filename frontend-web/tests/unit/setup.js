import { vi } from 'vitest'

// jsdom 未实现 matchMedia，Element Plus 部分组件会用到
if (!window.matchMedia) {
  window.matchMedia = (query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn()
  })
}

// jsdom 未实现 ResizeObserver，Element Plus 的 el-table / el-scrollbar 依赖它
if (!window.ResizeObserver) {
  window.ResizeObserver = class {
    observe() {}
    unobserve() {}
    disconnect() {}
  }
}
