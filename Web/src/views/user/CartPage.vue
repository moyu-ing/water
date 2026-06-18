<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api'

const router = useRouter()
const cartItems = ref([])
const addresses = ref([])
const addressId = ref()
const creating = ref(false)

const totalAmount = computed(() =>
  cartItems.value
    .filter((item) => item.checked === 1)
    .reduce((sum, item) => sum + Number(item.product?.price || 0) * item.quantity, 0)
    .toFixed(2),
)

async function loadData() {
  cartItems.value = await userApi.cart()
  addresses.value = await userApi.addresses()
  addressId.value = addresses.value.find((item) => item.isDefault === 1)?.id || addresses.value[0]?.id
}

async function updateItem(item) {
  await userApi.updateCart(item.id, {
    productId: item.productId,
    quantity: item.quantity,
    checked: item.checked,
  })
  ElMessage.success('购物车已更新')
}

async function removeItem(id) {
  await userApi.deleteCart(id)
  ElMessage.success('已删除')
  await loadData()
}

async function createOrder() {
  if (!addressId.value) {
    ElMessage.warning('请先维护收货地址')
    router.push('/profile')
    return
  }
  creating.value = true
  try {
    await userApi.createOrder({ addressId: addressId.value, remark: '网页下单' })
    ElMessage.success('订单已创建')
    router.push('/orders')
  } finally {
    creating.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-shell cart-page">
    <div class="cart-grid">
      <section class="soft-card cart-list">
        <div class="section-title">
          <h2>购物车</h2>
          <span>{{ cartItems.length }} 件商品</span>
        </div>
        <div v-if="cartItems.length" class="cart-items">
          <article v-for="item in cartItems" :key="item.id" class="cart-item">
            <img :src="item.product?.image" :alt="item.product?.name" />
            <div class="cart-info">
              <h3>{{ item.product?.name }}</h3>
              <p>{{ item.product?.spec }}</p>
              <strong>¥ {{ item.product?.price }}</strong>
            </div>
            <el-switch v-model="item.checked" :active-value="1" :inactive-value="0" @change="updateItem(item)" />
            <el-input-number v-model="item.quantity" :min="1" @change="updateItem(item)" />
            <el-button text type="danger" @click="removeItem(item.id)">删除</el-button>
          </article>
        </div>
        <el-empty v-else description="购物车还是空的，先去挑选几桶水吧" />
      </section>

      <aside class="soft-card order-panel">
        <h3>确认订单</h3>
        <p>请选择收货地址后提交订单，当前默认支付方式为货到付款。</p>
        <el-select v-model="addressId" placeholder="选择收货地址" style="width: 100%">
          <el-option
            v-for="item in addresses"
            :key="item.id"
            :label="`${item.contactName} ${item.contactPhone} ${item.province}${item.city}${item.district}${item.detailAddress}`"
            :value="item.id"
          />
        </el-select>
        <div class="order-total">
          <span>合计</span>
          <strong>¥ {{ totalAmount }}</strong>
        </div>
        <el-button type="primary" size="large" :loading="creating" @click="createOrder">提交订单</el-button>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
  padding: 24px 0 36px;
}

.cart-grid {
  display: grid;
  grid-template-columns: 1.8fr 1fr;
  gap: 22px;
}

.cart-list,
.order-panel {
  padding: 22px;
}

.cart-items {
  display: grid;
  gap: 16px;
}

.cart-item {
  display: grid;
  grid-template-columns: 110px 1fr auto auto auto;
  gap: 16px;
  align-items: center;
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.cart-item img {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 16px;
}

.cart-info h3 {
  margin: 0 0 8px;
}

.cart-info p {
  margin: 0 0 6px;
  color: var(--muted);
}

.cart-info strong {
  color: var(--brand);
}

.order-panel h3 {
  margin-top: 0;
}

.order-total {
  display: flex;
  justify-content: space-between;
  margin: 18px 0;
  font-size: 18px;
}

.order-total strong {
  color: var(--brand);
  font-size: 28px;
}

@media (max-width: 1000px) {
  .cart-grid {
    grid-template-columns: 1fr;
  }

  .cart-item {
    grid-template-columns: 90px 1fr;
  }
}
</style>
