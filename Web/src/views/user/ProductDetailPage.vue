<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publicApi, userApi } from '../../api'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref(null)
const quantity = ref(1)

async function loadDetail() {
  product.value = await publicApi.productDetail(route.params.id)
}

async function addCart() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  await userApi.addCart({ productId: product.value.id, quantity: quantity.value, checked: 1 })
  ElMessage.success({
    message: '已加入购物车',
    duration: 1000,
  })
}

onMounted(loadDetail)
</script>

<template>
  <div class="page-shell detail-page" v-if="product">
    <div class="soft-card detail-card">
      <img class="detail-image" :src="product.image" :alt="product.name" />
      <div class="detail-content">
        <span class="product-tag">{{ product.categoryName }}</span>
        <h1>{{ product.name }}</h1>
        <p class="sub-title">{{ product.subTitle }}</p>
        <p class="description">{{ product.description }}</p>
        <div class="detail-meta">
          <span>规格：{{ product.spec }}</span>
          <span>库存：{{ product.stock }}</span>
        </div>
        <div class="buy-bar">
          <strong>¥ {{ product.price }}</strong>
          <el-input-number v-model="quantity" :min="1" :max="product.stock || 99" />
          <el-button type="primary" size="large" @click="addCart">加入购物车</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  padding: 24px 0 36px;
}

.detail-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 28px;
  padding: 24px;
}

.detail-image {
  width: 100%;
  border-radius: 24px;
  aspect-ratio: 1 / 1;
  object-fit: cover;
}

.product-tag {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(15, 108, 191, 0.1);
  color: var(--brand);
}

.detail-content h1 {
  margin: 14px 0 10px;
}

.sub-title,
.description {
  color: var(--muted);
}

.detail-meta {
  display: flex;
  gap: 18px;
  margin: 20px 0;
}

.buy-bar {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.buy-bar strong {
  font-size: 34px;
  color: var(--brand);
}

@media (max-width: 900px) {
  .detail-card {
    grid-template-columns: 1fr;
  }
}
</style>
