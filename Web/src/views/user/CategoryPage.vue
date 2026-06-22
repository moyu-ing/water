<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publicApi, userApi } from '../../api'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const categories = ref([])
const products = ref([])
const keyword = ref(route.query.keyword || '')

const currentCategoryId = computed(() => Number(route.query.categoryId || 0))

async function loadCategories() {
  categories.value = await publicApi.categories()
}

async function loadProducts() {
  products.value = await publicApi.products({
    categoryId: currentCategoryId.value || undefined,
    keyword: keyword.value || undefined,
  })
}

function filterByCategory(id) {
  router.push({ path: '/category', query: id ? { categoryId: id } : {} })
}

async function addCart(item) {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  await userApi.addCart({ productId: item.id, quantity: 1, checked: 1 })
  ElMessage.success({
    message: '已加入购物车',
    duration: 1000,
  })
}

function search() {
  router.push({ path: '/category', query: { ...route.query, keyword: keyword.value || undefined } })
}

onMounted(async () => {
  await loadCategories()
  await loadProducts()
})

watch(() => route.query, loadProducts, { deep: true })
</script>

<template>
  <div class="page-shell listing-page">
    <div class="soft-card listing-header">
      <div class="section-title">
        <h2>全部商品</h2>
        <el-input v-model="keyword" placeholder="搜索商品名称" style="max-width: 280px" @keyup.enter="search">
          <template #append>
            <el-button @click="search">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div class="filter-row">
        <button class="filter-btn" :class="{ active: !currentCategoryId }" @click="filterByCategory(0)">全部</button>
        <button
          v-for="item in categories"
          :key="item.id"
          class="filter-btn"
          :class="{ active: currentCategoryId === item.id }"
          @click="filterByCategory(item.id)"
        >
          {{ item.name }}
        </button>
      </div>
    </div>

    <div class="product-grid">
      <article v-for="item in products" :key="item.id" class="soft-card product-card">
        <router-link :to="`/product/${item.id}`">
          <img :src="item.image" :alt="item.name" />
        </router-link>
        <div class="product-content">
          <span class="product-tag">{{ item.categoryName }}</span>
          <h3>{{ item.name }}</h3>
          <p>{{ item.subTitle }}</p>
          <div class="product-bottom">
            <div>
              <strong>¥ {{ item.price }}</strong>
              <small>{{ item.spec }}</small>
            </div>
            <el-button type="primary" @click="addCart(item)">加入购物车</el-button>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.listing-page {
  padding: 20px 0 34px;
}

.listing-header {
  padding: 22px;
  margin-bottom: 22px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-btn {
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.8);
  cursor: pointer;
}

.filter-btn.active {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, var(--brand), var(--brand-2));
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 18px;
}

.product-card {
  overflow: hidden;
}

.product-card img {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
}

.product-content {
  padding: 18px;
}

.product-tag {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(15, 108, 191, 0.1);
  color: var(--brand);
  font-size: 12px;
}

.product-content h3 {
  margin: 12px 0 6px;
}

.product-content p {
  margin: 0 0 16px;
  color: var(--muted);
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.product-bottom strong {
  display: block;
  color: var(--brand);
  font-size: 22px;
}

.product-bottom small {
  color: var(--muted);
}
</style>
