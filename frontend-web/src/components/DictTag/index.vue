<template>
  <el-tag v-if="label" :type="tagType" :disable-transitions="true">{{ label }}</el-tag>
  <span v-else class="text-muted">—</span>
</template>

<script setup>
import { computed } from 'vue'

defineOptions({ name: 'DictTag' })

const props = defineProps({
  // 字典数组，形如 [{ value, label, type }]
  options: {
    type: Array,
    default: () => []
  },
  // 当前值
  value: {
    type: [String, Number],
    default: ''
  }
})

const hit = computed(() => props.options.find((item) => String(item.value) === String(props.value)))

const label = computed(() => (hit.value ? hit.value.label : ''))
const tagType = computed(() => (hit.value ? hit.value.type : 'info'))
</script>
