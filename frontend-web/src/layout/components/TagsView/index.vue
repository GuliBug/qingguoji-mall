<template>
  <div class="tags-view-container">
    <el-scrollbar class="tags-view-wrapper">
      <div class="tags-view-inner">
        <router-link
          v-for="tag in visitedViews"
          :key="tag.fullPath"
          :to="tag.fullPath"
          class="tags-view-item"
          :class="{ active: isActive(tag) }"
        >
          {{ tag.title }}
          <el-icon v-if="!tag.affix" class="close-icon" @click.prevent.stop="closeTag(tag)">
            <Close />
          </el-icon>
        </router-link>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Close } from '@element-plus/icons-vue'

defineOptions({ name: 'TagsView' })

const route = useRoute()
const router = useRouter()

const visitedViews = ref([])

/**
 * 判断页签是否为当前激活项
 * @param {object} tag
 * @returns {boolean}
 */
function isActive(tag) {
  return tag.fullPath === route.fullPath
}

/**
 * 添加页签（去重）
 */
function addView() {
  if (!route.meta || !route.meta.title) {
    return
  }
  const fullPath = route.fullPath
  if (visitedViews.value.some((v) => v.fullPath === fullPath)) {
    return
  }
  visitedViews.value.push({
    title: route.meta.title,
    fullPath,
    affix: route.meta.affix === true
  })
}

/**
 * 关闭页签
 * @param {object} tag
 */
function closeTag(tag) {
  const index = visitedViews.value.findIndex((v) => v.fullPath === tag.fullPath)
  visitedViews.value.splice(index, 1)
  if (isActive(tag)) {
    const last = visitedViews.value[visitedViews.value.length - 1]
    router.push(last ? last.fullPath : '/index')
  }
}

watch(
  () => route.fullPath,
  () => addView(),
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.tags-view-container {
  height: 34px;
  width: 100%;
  background: #fff;
  border-bottom: 1px solid #d8dce5;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.06);

  .tags-view-inner {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 0 12px;
    height: 34px;
    white-space: nowrap;
  }

  .tags-view-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    height: 24px;
    padding: 0 8px;
    font-size: 12px;
    color: #495060;
    background: #fff;
    border: 1px solid #d8dce5;
    border-radius: 3px;

    &.active {
      background-color: #409eff;
      color: #fff;
      border-color: #409eff;

      &::before {
        content: '';
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #fff;
        display: inline-block;
        margin-right: 2px;
      }
    }

    .close-icon {
      font-size: 12px;
      border-radius: 50%;

      &:hover {
        background: rgba(0, 0, 0, 0.16);
        color: #fff;
      }
    }
  }
}
</style>
