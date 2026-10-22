<template>
  <div class="error-page">
    <div class="error-code">404</div>
    <div class="error-desc">抱歉，您访问的页面不存在</div>
    <el-button type="primary" @click="$router.push('/index')">返回首页</el-button>
  </div>
</template>

<script setup>
defineOptions({ name: 'NotFound' })
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: 16px;
  background: #f0f2f5;
}

.error-code {
  font-size: 90px;
  font-weight: 700;
  color: #409eff;
  line-height: 1;
}

.error-desc {
  font-size: 16px;
  color: #606266;
}
</style>
