<template>
  <div class="sidebar-logo">
    <router-link to="/">
      <span class="logo-title">青果纪 · 商城管理</span>
    </router-link>
  </div>
</template>

<script setup>
defineOptions({ name: 'SidebarLogo' })

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})
</script>
