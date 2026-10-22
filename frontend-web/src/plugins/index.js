import modal from './modal'
import cache from './cache'
import { download } from '@/utils/request'

/**
 * 挂载全局插件（若依风格：this.$modal / this.$cache）
 * @param {import('vue').App} app
 */
export default function plugins(app) {
  app.config.globalProperties.$modal = modal
  app.config.globalProperties.$cache = cache
  app.config.globalProperties.$download = download
}
