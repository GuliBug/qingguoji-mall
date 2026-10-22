<template>
  <div class="sidebar-container">
    <Logo :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        :collapse-transition="false"
        mode="vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <SidebarItem v-for="menu in sidebarRouters" :key="menu.path" :item="menu" :base-path="menu.path" />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Logo from './Logo.vue'
import SidebarItem from './SidebarItem.vue'
import useAppStore from '@/store/modules/app'
import usePermissionStore from '@/store/modules/permission'

defineOptions({ name: 'Sidebar' })

const route = useRoute()
const appStore = useAppStore()
const permissionStore = usePermissionStore()

const sidebarRouters = computed(() => permissionStore.sidebarRouters.filter((item) => !item.hidden))
const isCollapse = computed(() => !appStore.sidebar.opened)

// 当前高亮菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  :deep(.el-scrollbar) {
    height: calc(100% - 50px);
  }
}
</style>
