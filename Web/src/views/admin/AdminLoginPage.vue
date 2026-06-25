<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '../../stores/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123',
})

async function submit() {
  if (!form.username.trim()) {
    ElMessage.warning('请输入管理员账号')
    return
  }
  if (!form.password) {
    ElMessage.warning('请输入密码')
    return
  }
  loading.value = true
  try {
    await adminStore.login(form)
    ElMessage.success('管理员登录成功')
    router.push(route.query.redirect || '/admin/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '管理员登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="admin-login-page">
    <div class="soft-card admin-login-card">
      <div class="login-brand">
        <span>默认演示账号：admin / admin123</span>
        <h1>桶装水配送商城后台</h1>
        <p>覆盖商品、分类、订单、管理员、角色和菜单权限的统一管理入口。</p>
      </div>
      <el-form label-position="top">
        <el-form-item label="管理员账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">登录后台</el-button>
        <router-link class="go-mall" to="/">返回商城首页</router-link>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 20px;
}

.admin-login-card {
  width: min(480px, 100%);
  padding: 30px;
}

.login-brand {
  margin-bottom: 20px;
}

.login-brand span {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(15, 108, 191, 0.1);
  color: var(--brand);
  font-weight: 700;
}

.login-brand h1 {
  margin: 14px 0 10px;
}

.login-brand p,
.go-mall {
  color: var(--muted);
}

.go-mall {
  display: block;
  margin-top: 18px;
  text-align: center;
}
</style>
