import { defineStore } from 'pinia'
import Cookies from 'js-cookie'

const useAppStore = defineStore('app', {
  state: () => ({
    sidebar: {
      opened: Cookies.get('sidebarStatus') ? !!+Cookies.get('sidebarStatus') : true,
      withoutAnimation: false
    },
    device: 'desktop'
  }),
  actions: {
    /**
     * 切换侧边栏折叠状态
     */
    toggleSideBar() {
      this.sidebar.opened = !this.sidebar.opened
      this.sidebar.withoutAnimation = false
      Cookies.set('sidebarStatus', this.sidebar.opened ? 1 : 0)
    },
    /**
     * 关闭侧边栏（移动端）
     */
    closeSideBar(withoutAnimation) {
      Cookies.set('sidebarStatus', 0)
      this.sidebar.opened = false
      this.sidebar.withoutAnimation = withoutAnimation
    }
  }
})

export default useAppStore
