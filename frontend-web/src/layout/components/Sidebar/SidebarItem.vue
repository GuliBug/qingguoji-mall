<template>
  <!-- 只有一个子路由且未被隐藏时，直接渲染为一级菜单 -->
  <template v-if="hasOneShowingChild(item.children, item) && !onlyOneChild.alwaysShow">
    <el-menu-item :index="resolvePath(onlyOneChild.path)" @click="handleClick">
      <el-icon v-if="onlyOneChild.meta && onlyOneChild.meta.icon">
        <component :is="onlyOneChild.meta.icon" />
      </el-icon>
      <template #title>
        <span class="menu-title">{{ onlyOneChild.meta && onlyOneChild.meta.title }}</span>
      </template>
    </el-menu-item>
  </template>

  <!-- 多个子路由时渲染为可展开的子菜单 -->
  <el-sub-menu v-else :index="resolvePath(item.path)" teleported>
    <template #title>
      <el-icon v-if="item.meta && item.meta.icon">
        <component :is="item.meta.icon" />
      </el-icon>
      <span class="menu-title">{{ item.meta && item.meta.title }}</span>
    </template>
    <SidebarItem
      v-for="child in visibleChildren"
      :key="child.path"
      :item="child"
      :base-path="resolvePath(child.path)"
    />
  </el-sub-menu>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { isExternal } from '@/utils/validate'

defineOptions({ name: 'SidebarItem' })

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const onlyOneChild = ref(null)

const visibleChildren = computed(() => (props.item.children || []).filter((child) => !child.hidden))

/**
 * 判断是否只有一个可见子路由（若依同款逻辑）
 * @param {Array} children
 * @param {object} parent
 * @returns {boolean}
 */
function hasOneShowingChild(children = [], parent) {
  const showingChildren = (children || []).filter((child) => !child.hidden)
  if (showingChildren.length === 1) {
    onlyOneChild.value = { ...showingChildren[0], path: showingChildren[0].path, meta: showingChildren[0].meta }
    return true
  }
  if (showingChildren.length === 0) {
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
    return true
  }
  return false
}

/**
 * 拼接完整路径
 * @param {string} routePath
 * @returns {string}
 */
function resolvePath(routePath) {
  if (isExternal(routePath)) {
    return routePath
  }
  if (isExternal(props.basePath)) {
    return props.basePath
  }
  if (!routePath) {
    return props.basePath
  }
  return (props.basePath + '/' + routePath).replace(/\/+/g, '/')
}

/**
 * 点击菜单跳转
 */
function handleClick() {
  const path = resolvePath(onlyOneChild.value ? onlyOneChild.value.path : '')
  if (isExternal(path)) {
    window.open(path)
    return
  }
  if (router.currentRoute.value.path !== path) {
    router.push(path)
  }
}
</script>

<style scoped>
.menu-title {
  margin-left: 2px;
}
</style>
