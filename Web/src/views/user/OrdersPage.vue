<script setup>
import { onMounted, ref } from 'vue'
import { userApi } from '../../api'

const orders = ref([])

async function loadOrders() {
  orders.value = await userApi.orders()
}

onMounted(loadOrders)
</script>

<template>
  <div class="page-shell orders-page">
    <div class="section-title">
      <h2>我的订单</h2>
    </div>
    <div class="orders-list">
      <article v-for="order in orders" :key="order.id" class="soft-card order-card">
        <div class="order-head">
          <div>
            <strong>{{ order.orderNo }}</strong>
            <p>{{ order.contactName }} · {{ order.contactPhone }}</p>
          </div>
          <el-tag>{{ order.status }}</el-tag>
        </div>
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <span>{{ item.productName }}</span>
            <span>{{ item.quantity }} x ¥{{ item.productPrice }}</span>
          </div>
        </div>
        <div class="order-foot">
          <span>{{ order.fullAddress }}</span>
          <strong>合计 ¥ {{ order.totalAmount }}</strong>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.orders-page {
  padding: 24px 0 36px;
}

.orders-list {
  display: grid;
  gap: 18px;
}

.order-card {
  padding: 22px;
}

.order-head,
.order-foot,
.order-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.order-head p {
  color: var(--muted);
  margin: 6px 0 0;
}

.order-items {
  margin: 16px 0;
  display: grid;
  gap: 8px;
}

.order-foot {
  color: var(--muted);
}

.order-foot strong {
  color: var(--brand);
}
</style>
