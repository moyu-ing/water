<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../../api'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const cartItems = ref([])
const addresses = ref([])
const addressId = ref()
const creating = ref(false)
const availableCoupons = ref([])
const selectedCouponId = ref(null)
const usePoints = ref(0)
const pointsBalance = ref(0)

const totalAmount = computed(() =>
  cartItems.value
    .filter((item) => item.checked === 1)
    .reduce((sum, item) => sum + Number(item.product?.price || 0) * item.quantity, 0)
    .toFixed(2),
)

const barrelDepositTotal = computed(() =>
  cartItems.value
    .filter((item) => item.checked === 1 && item.product?.isBarrel === 1)
    .reduce((sum, item) => sum + Number(item.product?.barrelDeposit || 0) * item.quantity, 0)
    .toFixed(2),
)

const grandTotal = computed(() =>
  (Number(totalAmount.value) + Number(barrelDepositTotal.value)).toFixed(2),
)

const couponDiscountAmount = computed(() => {
  if (!selectedCouponId.value) return 0
  const coupon = availableCoupons.value.find(c => c.id === selectedCouponId.value)
  if (!coupon) return 0
  const subtotal = Number(grandTotal.value)
  if (subtotal < Number(coupon.minAmount || 0)) return 0
  if (coupon.type === 'FULL_REDUCTION') return Number(coupon.discountValue || 0)
  if (coupon.type === 'DISCOUNT') return subtotal * Number(coupon.discountValue || 0) / 100
  return 0
})

const pointsDiscountAmount = computed(() => {
  if (usePoints.value <= 0) return 0
  return Math.min(usePoints.value, pointsBalance.value) / 100
})

const finalTotal = computed(() => {
  const total = Number(grandTotal.value) - couponDiscountAmount.value - pointsDiscountAmount.value
  return Math.max(0, total).toFixed(2)
})

async function loadData() {
  try {
    cartItems.value = await userApi.cart()
    addresses.value = await userApi.addresses()
    addressId.value = addresses.value.find((item) => item.isDefault === 1)?.id || addresses.value[0]?.id
  } catch (e) {
    ElMessage.error('加载购物车数据失败: ' + (e.message || '网络错误'))
  }
  try {
    availableCoupons.value = await userApi.availableCoupons()
  } catch { availableCoupons.value = [] }
  try {
    const pts = await userApi.pointsBalance()
    pointsBalance.value = pts?.balance || 0
  } catch { pointsBalance.value = 0 }
}

async function updateItem(item) {
  try {
    await userApi.updateCart(item.id, {
      productId: item.productId,
      quantity: item.quantity,
      checked: item.checked,
    })
    ElMessage.success('购物车已更新')
  } catch (e) {
    ElMessage.error('更新购物车失败: ' + (e.message || '网络错误'))
  }
}

async function removeItem(id) {
  try {
    await ElMessageBox.confirm('确定要从购物车移除该商品吗？', '确认移除', { type: 'warning' })
    await userApi.deleteCart(id)
    ElMessage.success('已删除')
    await loadData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除购物车商品失败: ' + (e.message || '网络错误'))
    }
  }
}

async function createOrder() {
  if (!addressId.value) {
    ElMessage.warning('请先维护收货地址')
    router.push('/profile')
    return
  }
  creating.value = true
  try {
    await userApi.createOrder({
      addressId: addressId.value,
      remark: '网页下单',
      userCouponId: selectedCouponId.value || undefined,
      usePoints: usePoints.value > 0 ? usePoints.value : undefined,
    })
    ElMessage.success('订单已创建')
    await userStore.refreshProfile()
    router.push('/orders')
  } catch (e) {
    ElMessage.error('创建订单失败: ' + (e.message || '网络错误'))
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
        <!-- 优惠券选择 -->
        <div v-if="availableCoupons.length" class="coupon-section">
          <el-select v-model="selectedCouponId" placeholder="选择优惠券" clearable style="width:100%;margin-top:14px">
            <el-option
              v-for="c in availableCoupons"
              :key="c.id"
              :label="`${c.name} (${c.type === 'FULL_REDUCTION' ? '减¥' + c.discountValue : c.discountValue + '%'})`"
              :value="c.id"
            />
          </el-select>
        </div>
        <!-- 积分使用 -->
        <div class="points-section">
          <span>⭐ 可用积分: {{ pointsBalance }}（100分=1元）</span>
          <el-input-number v-model="usePoints" :min="0" :max="pointsBalance" :step="100" placeholder="使用积分" style="width:100%;margin-top:8px" />
        </div>
        <div class="order-total">
          <div class="total-lines">
            <div class="total-line"><span>商品金额</span><span>¥ {{ totalAmount }}</span></div>
            <div class="total-line" v-if="barrelDepositTotal > 0"><span>🪣 桶押金</span><span>¥ {{ barrelDepositTotal }}</span></div>
            <div class="total-line" v-if="couponDiscountAmount > 0"><span>🎫 优惠券</span><span style="color:#14b8a6">-¥ {{ couponDiscountAmount.toFixed(2) }}</span></div>
            <div class="total-line" v-if="pointsDiscountAmount > 0"><span>⭐ 积分抵扣</span><span style="color:#14b8a6">-¥ {{ pointsDiscountAmount.toFixed(2) }}</span></div>
          </div>
          <div class="total-line grand"><span>合计</span><strong>¥ {{ finalTotal }}</strong></div>
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

.coupon-section, .points-section {
  margin-top: 8px;
}
.points-section span {
  font-size: 13px;
  color: var(--muted);
}
.order-total {
  margin: 18px 0;
}
.total-lines {
  margin-bottom: 12px;
}
.total-line {
  display: flex;
  justify-content: space-between;
  margin: 6px 0;
  color: var(--muted);
  font-size: 14px;
}
.total-line.grand {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
  font-size: 18px;
  color: #333;
}
.total-line.grand strong {
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
