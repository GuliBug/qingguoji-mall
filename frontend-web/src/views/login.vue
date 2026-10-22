<template>
  <div class="login">
    <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">青果纪商城管理系统</h3>

      <el-form-item prop="username">
        <el-input v-model="loginForm.username" type="text" auto-complete="off" placeholder="账号">
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          auto-complete="off"
          placeholder="密码"
          show-password
          @keyup.enter="handleLogin"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item style="width: 100%">
        <el-button :loading="loading" size="default" type="primary" style="width: 100%" @click.prevent="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
      </el-form-item>

      <div class="tips">默认账号：admin / admin123</div>
    </el-form>

    <div class="el-login-footer">
      <span>Copyright © 2026 青果纪 · 槟榔品牌会员商城</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'

defineOptions({ name: 'Login' })

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = ref({
  username: 'admin',
  password: 'admin123'
})

const loginRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入您的账号' }],
  password: [{ required: true, trigger: 'blur', message: '请输入您的密码' }]
}

/**
 * 登录
 */
function handleLogin() {
  loginFormRef.value.validate((valid) => {
    if (!valid) {
      return
    }
    loading.value = true
    userStore
      .login(loginForm.value)
      .then(() => {
        const redirect = route.query.redirect
        router.push({ path: redirect && redirect !== '/login' ? redirect : '/index' })
      })
      .catch((err) => {
        loading.value = false
        // 错误提示已由 request 拦截器统一弹出
        console.warn('[login] failed:', err && err.message ? err.message : err)
      })
  })
}
</script>

<style lang="scss" scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url('@/assets/images/login-background.svg');
  background-color: #2d3a4b;
  background-size: cover;
  background-position: center;
}

.title {
  margin: 0 auto 30px auto;
  text-align: center;
  color: #707070;
  letter-spacing: 1px;
  font-size: 20px;
  font-weight: 600;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  box-shadow: 0 0 25px rgba(0, 0, 0, 0.18);

  .el-input {
    height: 38px;
  }

  .tips {
    font-size: 12px;
    color: #909399;
    text-align: center;
    margin-bottom: 12px;
  }
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial, sans-serif;
  font-size: 12px;
  letter-spacing: 1px;
}
</style>
