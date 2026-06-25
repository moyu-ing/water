<script setup>
import { useDeliveryStore } from '../stores/delivery'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const deliveryStore = useDeliveryStore()
const router = useRouter()

function handleLogout() {
  deliveryStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<template>
  <div class="delivery-layout">
    <header class="delivery-header">
      <h2>🚚 配送员工作台</h2>
      <div class="header-right">
        <span v-if="deliveryStore.profile">{{ deliveryStore.profile.name }}</span>
        <el-button text @click="handleLogout">退出</el-button>
      </div>
    </header>
    <main class="delivery-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.delivery-layout {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.delivery-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 24px;
  background: linear-gradient(135deg, #0f6cbf, #14b8a6);
  color: #fff;
}

.delivery-header h2 {
  margin: 0;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.delivery-main {
  flex: 1;
  padding: 20px 24px;
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
}</style>
