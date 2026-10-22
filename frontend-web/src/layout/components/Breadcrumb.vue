<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in levelList" :key="item.path">
        <span v-if="index === levelList.length - 1" class="no-redirect">{{ item.meta.title }}</span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

defineOptions({ name: 'Breadcrumb' })

const route = useRoute()
const router = useRouter()
const levelList = ref([])

/**
 * 根据当前路由生成面包屑
 */
function getBreadcrumb() {
  const matched = route.matched.filter((item) => item.meta && item.meta.title)
  levelList.value = [{ path: '/index', meta: { title: '首页' } }, ...matched]
  // 首页自身不重复展示
  if (route.path === '/index' || route.path === '/') {
    levelList.value = [{ path: '/index', meta: { title: '首页' } }]
  }
}

/**
 * 点击面包屑跳转
 * @param {object} item
 */
function handleLink(item) {
  router.push(item.path)
}

watch(
  () => route.path,
  () => getBreadcrumb(),
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.app-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;

  .no-redirect {
    color: #97a8be;
    cursor: text;
  }

  a {
    color: #606266;
    cursor: pointer;
    font-weight: 400;

    &:hover {
      color: #409eff;
    }
  }
}
</style>
