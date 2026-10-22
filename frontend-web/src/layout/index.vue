<template>
  <div :class="classObj" class="app-wrapper">
    <div v-if="device === 'mobile' && sidebar.opened" class="drawer-bg" @click="handleClickOutside" />
    <Sidebar class="sidebar-container" />
    <div :class="{ hasTagsView: needTagsView }" class="main-container">
      <div :class="{ 'fixed-header': fixedHeader }">
        <Navbar />
        <TagsView v-if="needTagsView" />
      </div>
      <AppMain />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { AppMain, Navbar, Sidebar, TagsView } from './components'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'

defineOptions({ name: 'Layout' })

const appStore = useAppStore()
const settingsStore = useSettingsStore()

const sidebar = computed(() => appStore.sidebar)
const device = computed(() => appStore.device)
const needTagsView = computed(() => settingsStore.tagsView !== false)
const fixedHeader = computed(() => settingsStore.fixedHeader)

const classObj = computed(() => ({
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === 'mobile'
}))

/**
 * 移动端点击遮罩关闭侧边栏
 */
function handleClickOutside() {
  appStore.closeSideBar(false)
}

/**
 * 窗口尺寸变化时切换 desktop / mobile
 */
function handleResize() {
  appStore.device = window.innerWidth < 992 ? 'mobile' : 'desktop'
  if (appStore.device === 'mobile' && appStore.sidebar.opened) {
    appStore.closeSideBar(true)
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.app-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
}

.main-container {
  min-height: 100%;
  transition: margin-left 0.28s;
  margin-left: $base-sidebar-width;
  position: relative;
  background: $content-bg;
}

.fixed-header {
  position: sticky;
  top: 0;
  z-index: 9;
  width: 100%;
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}
</style>
