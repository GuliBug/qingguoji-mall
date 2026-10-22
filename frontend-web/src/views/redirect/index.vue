<template>
  <div class="redirect-wrap">
    <el-skeleton :rows="6" animated />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

defineOptions({ name: 'Redirect' })

const route = useRoute()
const router = useRouter()

onMounted(() => {
  const { params, query } = route
  const { path } = params
  const target = Array.isArray(path) ? path.join('/') : path
  router.replace({ path: '/' + target, query })
})
</script>

<style scoped>
.redirect-wrap {
  padding: 24px;
}
</style>
