import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
      extensions: ['.mjs', '.js', '.json', '.vue']
    },
    server: {
      port: 5173,
      host: '0.0.0.0',
      open: false,
      proxy: {
        // 与若依一致：开发期用代理转发 /admin-api，规避跨域
        [env.VITE_APP_BASE_API]: {
          target: env.VITE_APP_TARGET,
          changeOrigin: true
        }
      }
    },
    build: {
      outDir: 'dist',
      sourcemap: false,
      chunkSizeWarningLimit: 2000
    }
  }
})
