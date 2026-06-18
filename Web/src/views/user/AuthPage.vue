<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', nickname: '', phone: '' })
const loading = ref(false)

async function submitLogin() {
  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  loading.value = true
  try {
    await userApi.register(registerForm)
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = registerForm.password
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="soft-card auth-card">
      <div class="auth-copy">
        <span>欢迎来到泉到家</span>
        <h1>同一个前端项目，同时承载商城与后台管理。</h1>
        <p>用户端下单、后台端运营、角色权限和商品数据全部打通。</p>
      </div>
      <div class="auth-form-panel">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="登录" name="login">
            <el-form label-position="top">
              <el-form-item label="用户名"><el-input v-model="loginForm.username" /></el-form-item>
              <el-form-item label="密码"><el-input v-model="loginForm.password" type="password" show-password /></el-form-item>
              <el-button type="primary" :loading="loading" style="width: 100%" @click="submitLogin">登录</el-button>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
            <el-form label-position="top">
              <el-form-item label="用户名"><el-input v-model="registerForm.username" /></el-form-item>
              <el-form-item label="密码"><el-input v-model="registerForm.password" type="password" show-password /></el-form-item>
              <el-form-item label="昵称"><el-input v-model="registerForm.nickname" /></el-form-item>
              <el-form-item label="手机号"><el-input v-model="registerForm.phone" /></el-form-item>
              <el-button type="primary" :loading="loading" style="width: 100%" @click="submitRegister">注册</el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 20px;
}

.auth-card {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
  padding: 28px;
}

.auth-copy {
  padding: 18px;
}

.auth-copy span {
  color: var(--brand);
  font-weight: 700;
}

.auth-copy h1 {
  font-size: clamp(32px, 4vw, 52px);
  line-height: 1.08;
  margin: 14px 0;
}

.auth-copy p {
  color: var(--muted);
}

.auth-form-panel {
  padding: 18px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.74);
}

@media (max-width: 900px) {
  .auth-card {
    grid-template-columns: 1fr;
  }
}
</style>
