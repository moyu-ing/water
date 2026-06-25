<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { publicApi, userApi } from '../../api'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const categories = ref([])
const products = ref([])

async function loadData() {
  try {
    categories.value = await publicApi.categories()
    products.value = await publicApi.products()
  } catch (e) {
    ElMessage.error('加载首页数据失败: ' + (e.message || '网络错误'))
  }
}

async function addCart(product) {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    await userApi.addCart({ productId: product.id, quantity: 1, checked: 1 })
    ElMessage.success({
      message: '已加入购物车',
      duration: 1000,
    })
  } catch (e) {
    ElMessage.error('加入购物车失败: ' + (e.message || '网络错误'))
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell home-page">
    <section class="hero soft-card">
      <div class="hero-copy">
        <span class="eyebrow">桶装水配送商城</span>
        <h1>让公司和家庭的每一桶水，都能准时送到门口。</h1>
        <p>天然饮用水、矿泉水、纯净水和饮水配件一站下单，后台库存和订单同步管理。</p>
        <div class="hero-actions">
          <router-link class="hero-btn primary" to="/category">立即选水</router-link>
          <router-link class="hero-btn ghost" to="/admin/login">进入后台</router-link>
        </div>
      </div>
      <div class="hero-panel">
        <div class="stat-card">
          <strong>5 大分类</strong>
          <span>已内置基础商品数据</span>
        </div>
        <div class="stat-card">
          <strong>支持购物车 + 订单</strong>
          <span>可直接走完整下单流程</span>
        </div>
      </div>
    </section>

    <section class="content-section">
      <div class="section-title">
        <h2>热门分类</h2>
      </div>
      <div class="category-grid">
        <router-link
          v-for="item in categories"
          :key="item.id"
          class="soft-card category-card"
          :to="{ path: '/category', query: { categoryId: item.id } }"
        >
          <img :src="item.image" :alt="item.name" />
          <strong>{{ item.name }}</strong>
        </router-link>
      </div>
    </section>

    <section class="content-section">
      <div class="section-title">
        <h2>商品推荐</h2>
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
              <strong>¥ {{ item.price }}</strong>
              <div class="product-actions">
                <el-button plain @click="router.push(`/product/${item.id}`)">详情</el-button>
                <el-button type="primary" @click="addCart(item)">加购</el-button>
              </div>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  padding: 22px 0 38px;
}

.hero {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 24px;
  padding: 28px;
  overflow: hidden;
}

.eyebrow {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(20, 184, 166, 0.14);
  color: #0f766e;
  font-weight: 700;
  margin-bottom: 12px;
}

.hero h1 {
  margin: 0;
  font-size: clamp(34px, 5vw, 56px);
  line-height: 1.05;
  max-width: 12ch;
}

.hero p {
  margin: 18px 0 22px;
  color: var(--muted);
  font-size: 16px;
  max-width: 52ch;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.hero-btn {
  padding: 14px 18px;
  border-radius: 16px;
  font-weight: 700;
}

.hero-btn.primary {
  color: #fff;
  background: linear-gradient(135deg, var(--brand), var(--brand-2));
}

.hero-btn.ghost {
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.7);
}

.hero-panel {
  display: grid;
  gap: 16px;
  align-content: center;
}

.stat-card {
  padding: 20px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(15, 108, 191, 0.1), rgba(20, 184, 166, 0.08));
  border: 1px solid rgba(15, 108, 191, 0.12);
}

.stat-card strong {
  display: block;
  font-size: 24px;
  margin-bottom: 8px;
}

.content-section {
  margin-top: 28px;
}

.category-grid,
.product-grid {
  display: grid;
  gap: 18px;
}

.category-grid {
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
}

.category-card {
  padding: 14px;
}

.category-card img,
.product-card img {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  border-radius: 18px;
}

.category-card strong {
  display: block;
  margin-top: 12px;
  font-size: 17px;
}

.product-grid {
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.product-card {
  overflow: hidden;
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
  margin: 12px 0 8px;
}

.product-content p {
  margin: 0 0 16px;
  color: var(--muted);
  min-height: 42px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
}

.product-bottom strong {
  font-size: 24px;
  color: var(--brand);
}

.product-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
