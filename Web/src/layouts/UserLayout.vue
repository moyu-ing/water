<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publicApi } from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const categories = ref([])

const activePath = computed(() => route.path)

async function loadCategories() {
  try {
    categories.value = await publicApi.categories()
  } catch (e) {
    ElMessage.error('加载分类导航失败: ' + (e.message || '网络错误'))
  }
}

function logout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

onMounted(loadCategories)
</script>

<template>
  <div>
    <header class="user-header">
      <div class="page-shell header-inner">
        <router-link class="brand-mark" to="/">
          <span class="brand-badge">W</span>
          <div>
            <strong>泉到家</strong>
            <p>桶装水配送商城</p>
          </div>
        </router-link>
        <nav class="main-nav">
          <router-link :class="{ active: activePath === '/' }" to="/">首页</router-link>
          <router-link :class="{ active: activePath === '/category' }" to="/category">全部商品</router-link>
          <router-link :class="{ active: activePath === '/cart' }" to="/cart">购物车</router-link>
          <router-link :class="{ active: activePath === '/orders' }" to="/orders">我的订单</router-link>
        </nav>
        <div class="header-actions">
          <router-link class="admin-entry" to="/admin/login">后台管理</router-link>
          <template v-if="userStore.isLoggedIn">
            <router-link class="profile-pill" to="/profile">{{ userStore.profile?.nickname || userStore.profile?.username }}</router-link>
            <el-button text @click="logout">退出</el-button>
          </template>
          <router-link v-else class="profile-pill" to="/login">登录 / 注册</router-link>
        </div>
      </div>
    </header>

    <section class="category-strip">
      <div class="page-shell category-strip-inner">
        <router-link
          v-for="category in categories"
          :key="category.id"
          class="category-chip"
          :to="{ path: '/category', query: { categoryId: category.id } }"
        >
          {{ category.name }}
        </router-link>
      </div>
    </section>

    <main>
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.user-header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(248, 252, 255, 0.84);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 0;
}

.brand-mark {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark strong {
  font-size: 20px;
}

.brand-mark p {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--muted);
}

.brand-badge {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  color: #fff;
  font-weight: 700;
  background: linear-gradient(135deg, var(--brand), var(--brand-2));
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 24px;
}

.main-nav a {
  color: var(--muted);
  font-weight: 600;
}

.main-nav a.active,
.main-nav a:hover {
  color: var(--brand);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.profile-pill,
.admin-entry {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid var(--line);
}

.category-strip {
  padding: 12px 0 4px;
}

.category-strip-inner {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-chip {
  padding: 9px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.18);
  color: var(--muted);
}

.category-chip:hover {
  color: var(--brand);
  border-color: rgba(15, 108, 191, 0.3);
}

@media (max-width: 900px) {
  .header-inner {
    flex-direction: column;
    align-items: flex-start;
  }

  .main-nav,
  .header-actions {
    flex-wrap: wrap;
  }
}
</style>
