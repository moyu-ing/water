<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../../api'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const addresses = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 1,
})

async function loadAddresses() {
  try {
    addresses.value = await userApi.addresses()
  } catch (e) {
    ElMessage.error('加载地址列表失败: ' + (e.message || '网络错误'))
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    contactName: '',
    contactPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    isDefault: 1,
  })
  dialogVisible.value = true
}

function openEdit(item) {
  editingId.value = item.id
  Object.assign(form, item)
  dialogVisible.value = true
}

async function submit() {
  if (!form.contactName.trim()) {
    ElMessage.warning('请输入联系人姓名')
    return
  }
  if (!form.contactPhone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.contactPhone.trim())) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (!form.detailAddress.trim()) {
    ElMessage.warning('请输入详细地址')
    return
  }
  try {
    if (editingId.value) {
      await userApi.updateAddress(editingId.value, form)
      ElMessage.success('地址已更新')
    } else {
      await userApi.saveAddress(form)
      ElMessage.success('地址已添加')
    }
    dialogVisible.value = false
    await loadAddresses()
  } catch (e) {
    ElMessage.error('保存地址失败: ' + (e.message || '网络错误'))
  }
}

async function removeAddress(id) {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '确认删除', { type: 'warning' })
    await userApi.deleteAddress(id)
    ElMessage.success('地址已删除')
    await loadAddresses()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除地址失败: ' + (e.message || '网络错误'))
    }
  }
}

onMounted(async () => {
  await userStore.refreshProfile()
  await loadAddresses()
})
</script>

<template>
  <div class="page-shell profile-page">
    <section class="soft-card profile-card">
      <div>
        <h2>{{ userStore.profile?.nickname }}</h2>
        <p>账号：{{ userStore.profile?.username }}</p>
        <p>手机号：{{ userStore.profile?.phone }}</p>
        <div class="profile-stats">
          <div class="stat-badge">🪣 持有空桶: {{ userStore.profile?.barrelCount || 0 }} 个</div>
          <div class="stat-badge">⭐ 可用积分: {{ userStore.profile?.points || 0 }} 分</div>
        </div>
      </div>
    </section>

    <section class="soft-card address-card">
      <div class="section-title">
        <h3>收货地址</h3>
        <el-button type="primary" @click="openCreate">新增地址</el-button>
      </div>
      <div class="address-grid">
        <article v-for="item in addresses" :key="item.id" class="address-item">
          <div class="address-top">
            <strong>{{ item.contactName }}</strong>
            <el-tag v-if="item.isDefault === 1" type="success">默认</el-tag>
          </div>
          <p>{{ item.contactPhone }}</p>
          <p>{{ item.province }}{{ item.city }}{{ item.district }}{{ item.detailAddress }}</p>
          <div class="address-actions">
            <el-button text @click="openEdit(item)">编辑</el-button>
            <el-button text type="danger" @click="removeAddress(item.id)">删除</el-button>
          </div>
        </article>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑地址' : '新增地址'" width="560px">
      <el-form label-width="88px">
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="省份"><el-input v-model="form.province" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="form.city" /></el-form-item>
        <el-form-item label="区县"><el-input v-model="form.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.detailAddress" type="textarea" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 24px 0 36px;
  display: grid;
  gap: 20px;
}

.profile-card,
.address-card {
  padding: 24px;
}

.profile-card p {
  color: var(--muted);
}
.profile-stats {
  display: flex;
  gap: 16px;
  margin-top: 14px;
}
.stat-badge {
  padding: 8px 16px;
  border-radius: 999px;
  background: rgba(15,108,191,0.08);
  color: var(--brand);
  font-weight: 600;
  font-size: 14px;
}

.address-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
}

.address-item {
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--line);
}

.address-top,
.address-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-item p {
  color: var(--muted);
}
</style>
