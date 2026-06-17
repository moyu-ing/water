<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAdminStore } from '../stores/admin'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()

const menuItems = computed(() => {
  const permissions = new Set(adminStore.permissions)
  return [
    { label: '首页', path: '/admin/dashboard', permission: 'admin:dashboard' },
    { label: '用户管理', path: '/admin/users', permission: 'user:view' },
    { label: '管理员', path: '/admin/admins', permission: 'admin:view' },
    { label: '角色管理', path: '/admin/roles', permission: 'role:view' },
    { label: '菜单权限', path: '/admin/menus', permission: 'menu:view' },
    { label: '分类管理', path: '/admin/categories', permission: 'category:view' },
    { label: '商品管理', path: '/admin/products', permission: 'product:view' },
    { label: '订单管理', path: '/admin/orders', permission: 'order:view' },
  ].filter((item) => permissions.has(item.permission))
})

function logout() {
  adminStore.logout()
  router.push('/admin/login')
}
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-aside">
      <div class="aside-brand">
        <strong>泉到家后台</strong>
        <span>单项目运营控制台</span>
      </div>
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        class="aside-link"
        :class="{ active: route.path === item.path }"
        :to="item.path"
      >
        {{ item.label }}
      </router-link>
    </aside>
    <div class="admin-main">
      <header class="admin-header">
        <div>
          <h2>{{ route.meta.title || '管理控制台' }}</h2>
          <p>{{ adminStore.profile?.nickname || adminStore.profile?.username }}</p>
        </div>
        <div class="admin-header-actions">
          <router-link to="/">返回商城</router-link>
          <el-button type="primary" plain @click="logout">退出登录</el-button>
        </div>
      </header>
      <section class="admin-content">
        <router-view />
      </section>
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  display: grid;
  grid-template-columns: 250px 1fr;
  min-height: 100vh;
  background: linear-gradient(160deg, #eff6ff 0%, #f8fafc 60%, #fdfdfd 100%);
}

.admin-aside {
  padding: 28px 18px;
  background: linear-gradient(180deg, #0f172a 0%, #102c4a 100%);
  color: #fff;
}

.aside-brand {
  margin-bottom: 28px;
}

.aside-brand strong {
  display: block;
  font-size: 22px;
  margin-bottom: 6px;
}

.aside-brand span {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}

.aside-link {
  display: block;
  padding: 12px 14px;
  border-radius: 14px;
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.75);
}

.aside-link.active,
.aside-link:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.admin-main {
  padding: 24px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.admin-header h2 {
  margin: 0 0 4px;
}

.admin-header p {
  margin: 0;
  color: var(--muted);
}

.admin-header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

@media (max-width: 960px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .admin-aside {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
}
</style>
