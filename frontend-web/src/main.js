import { createApp } from 'vue'

import Cookies from 'js-cookie'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import locale from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import '@/assets/styles/index.scss' // global css

import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive
import plugins from './plugins' // plugins
import { download } from '@/utils/request'

import './permission' // permission control

import { parseTime, addDateRange, handleTree, selectDictLabel } from '@/utils/ruoyi'
import { formatMoney, formatDateTime } from '@/utils/format'

// 分页组件
import Pagination from '@/components/Pagination'
// 自定义表格工具组件
import RightToolbar from '@/components/RightToolbar'
// 状态标签组件
import DictTag from '@/components/DictTag'

const app = createApp(App)

// 全局注册 element-plus 图标（菜单配置里以字符串引用图标名）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局方法挂载
app.config.globalProperties.download = download
app.config.globalProperties.parseTime = parseTime
app.config.globalProperties.addDateRange = addDateRange
app.config.globalProperties.handleTree = handleTree
app.config.globalProperties.selectDictLabel = selectDictLabel
app.config.globalProperties.formatMoney = formatMoney
app.config.globalProperties.formatDateTime = formatDateTime

// 全局组件挂载
app.component('DictTag', DictTag)
app.component('Pagination', Pagination)
app.component('RightToolbar', RightToolbar)

app.use(router)
app.use(store)
app.use(plugins)

directive(app)

// 使用 element-plus 并且设置全局的大小
app.use(ElementPlus, {
  locale: locale,
  // 支持 large、default、small
  size: Cookies.get('size') || 'default'
})

app.mount('#app')
