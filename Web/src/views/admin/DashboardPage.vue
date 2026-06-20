<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '../../api'

const stats = ref({
  users: 0,
  products: 0,
  categories: 0,
  orders: 0,
})

async function loadData() {
  const [users, products, categories, orders] = await Promise.all([
    adminApi.users(),
    adminApi.products(),
    adminApi.categories(),
    adminApi.orders(),
  ])
  stats.value = {
    users: users.length,
    products: products.length,
    categories: categories.length,
    orders: orders.length,
  }
}

onMounted(loadData)
</script>

<template>
  <div class="dashboard-grid">
    <section class="soft-card dashboard-card">
      <span>商城用户</span>
      <strong>{{ stats.users }}</strong>
    </section>
    <section class="soft-card dashboard-card">
      <span>商品数量</span>
      <strong>{{ stats.products }}</strong>
    </section>
    <section class="soft-card dashboard-card">
      <span>商品分类</span>
      <strong>{{ stats.categories }}</strong>
    </section>
    <section class="soft-card dashboard-card">
      <span>订单数量</span>
      <strong>{{ stats.orders }}</strong>
    </section>
  </div>
</template>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 18px;
}

.dashboard-card {
  padding: 24px;
}

.dashboard-card span {
  display: block;
  color: var(--muted);
  margin-bottom: 12px;
}

.dashboard-card strong {
  font-size: 40px;
  color: var(--brand);
}
</style>
