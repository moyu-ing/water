<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useDeliveryStore } from '../../stores/delivery'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const deliveryStore = useDeliveryStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!form.password) {
    ElMessage.warning('请输入密码')
    return
  }
  loading.value = true
  try {
    await deliveryStore.login(form)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/delivery'
    router.push(redirect)
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="soft-card login-card">
      <h2>🚚 配送员登录</h2>
      <el-form label-width="0" @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="form.username" placeholder="用户名" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" show-password /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width:100%">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f6cbf, #14b8a6);
}

.login-card {
  padding: 36px;
  width: 400px;
  max-width: 90vw;
}

.login-card h2 {
  margin: 0 0 24px;
  text-align: center;
}
</style>
