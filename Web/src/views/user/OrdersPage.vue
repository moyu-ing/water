<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api'

const orders = ref([])

async function loadOrders() {
  try {
    orders.value = await userApi.orders()
  } catch (e) {
    ElMessage.error('加载订单列表失败: ' + (e.message || '网络错误'))
  }
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
          <div>
            <span>{{ order.fullAddress }}</span>
            <div class="order-extras">
              <span v-if="order.barrelDepositAmount > 0">🪣 桶押金: ¥{{ order.barrelDepositAmount }}</span>
              <span v-if="order.barrelReturnCount > 0">🔄 回收空桶: {{ order.barrelReturnCount }}个</span>
              <span v-if="order.couponDiscount > 0">🎫 优惠券: -¥{{ order.couponDiscount }}</span>
              <span v-if="order.pointsDiscount > 0">⭐ 积分抵扣: -¥{{ order.pointsDiscount }}</span>
              <span v-if="order.pointsEarned > 0">✨ 获得积分: +{{ order.pointsEarned }}</span>
            </div>
          </div>
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
.order-extras {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
}
.order-extras span {
  color: var(--muted);
}
</style>
