import { defineStore } from 'pinia'
import defaultSettings from '@/settings'

const { sideTheme, title, dynamicTitle } = defaultSettings

const useSettingsStore = defineStore('settings', {
  state: () => ({
    title: title,
    theme: '#409EFF',
    sideTheme: sideTheme,
    dynamicTitle: dynamicTitle
  }),
  actions: {
    /**
     * 设置页面标题
     * @param {string} title
     */
    setTitle(title) {
      this.title = title
    }
  }
})

export default useSettingsStore
