<template>
  <div class="navbar">
    <div class="hamburger-container" @click="toggleSideBar">
      <el-icon :size="20">
        <Fold v-if="appStore.sidebar.opened" />
        <Expand v-else />
      </el-icon>
    </div>

    <breadcrumb class="breadcrumb-container" />

    <div class="right-menu">
      <div class="right-menu-item">{{ today }}</div>
      <el-dropdown class="avatar-container" trigger="click" @command="handleCommand">
        <div class="avatar-wrapper">
          <el-avatar :size="30">{{ (userStore.name || 'A').charAt(0).toUpperCase() }}</el-avatar>
          <span class="user-name">{{ userStore.name || 'admin' }}</span>
          <el-icon><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="home">首页看板</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Fold, Expand, CaretBottom } from '@element-plus/icons-vue'
import Breadcrumb from './Breadcrumb.vue'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'

defineOptions({ name: 'Navbar' })

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const today = computed(() => {
  const d = new Date()
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
})

/**
 * 折叠/展开侧边栏
 */
function toggleSideBar() {
  appStore.toggleSideBar()
}

/**
 * 下拉菜单命令
 * @param {string} command
 */
function handleCommand(command) {
  if (command === 'home') {
    router.push('/index')
    return
  }
  if (command === 'logout') {
    ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => userStore.logOut())
      .then(() => {
        router.push('/login')
      })
      .catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.navbar {
  height: $navbar-height;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;

  .hamburger-container {
    line-height: $navbar-height;
    height: 100%;
    padding: 0 15px;
    cursor: pointer;
    display: flex;
    align-items: center;
    transition: background 0.3s;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .breadcrumb-container {
    flex: 1;
  }

  .right-menu {
    display: flex;
    align-items: center;
    padding-right: 16px;
    gap: 16px;

    .right-menu-item {
      color: #5a5e66;
      font-size: 13px;
    }
  }

  .avatar-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    outline: none;
  }

  .user-name {
    font-size: 13px;
    color: #303133;
  }
}
</style>
