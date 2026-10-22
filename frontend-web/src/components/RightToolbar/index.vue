<template>
  <div class="top-right-btn" :style="style">
    <el-row>
      <el-tooltip class="item" effect="dark" :content="showSearch ? '隐藏搜索' : '显示搜索'" placement="top">
        <el-button circle :icon="Search" @click="toggleSearch" />
      </el-tooltip>
      <el-tooltip class="item" effect="dark" content="刷新" placement="top">
        <el-button circle :icon="Refresh" @click="refresh" />
      </el-tooltip>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

defineOptions({ name: 'RightToolbar' })

const props = defineProps({
  showSearch: {
    type: Boolean,
    default: true
  },
  columns: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:showSearch', 'queryTable'])

const style = computed(() => ({ float: 'right' }))

/**
 * 切换搜索区显示
 */
function toggleSearch() {
  emit('update:showSearch', !props.showSearch)
}

/**
 * 触发父组件重新查询
 */
function refresh() {
  emit('queryTable')
}
</script>

<style scoped>
.top-right-btn {
  display: flex;
  gap: 8px;
}
</style>
