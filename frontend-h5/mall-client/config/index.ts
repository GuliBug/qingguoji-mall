import { defineConfig, type UserConfigExport } from '@tarojs/cli'

export default defineConfig(async (merge, { command, mode }) => {
  const baseConfig: UserConfigExport = {
    projectName: 'mall-client',
    date: '2026-10-21',
    designWidth: 750,
    deviceRatio: {
      640: 2.34 / 2,
      750: 1,
      375: 2,
      828: 1.81 / 2
    },
    sourceRoot: 'src',
    outputRoot: 'dist',
    plugins: [],
    defineConstants: {},
    copy: { patterns: [], options: {} },
    framework: 'vue3',
    compiler: 'vite',
    mini: {},
    h5: {
      publicPath: '/',
      staticDirectory: 'static',
      router: { mode: 'hash' },
      devServer: { port: 10086 }
    }
  }
  return baseConfig
})
