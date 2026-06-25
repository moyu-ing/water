<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'

const rows = ref([])
const dialogVisible = ref(false)
const distributeVisible = ref(false)
const distributeForm = reactive({ templateId: null, userIds: '' })
const editingId = ref(null)
const form = reactive({
  name: '',
  type: 'FULL_REDUCTION',
  discountValue: 0,
  minAmount: 0,
  totalQuantity: 100,
  validDays: 30,
  status: 1,
})

async function loadData() {
  try {
    rows.value = await adminApi.coupons()
  } catch (e) {
    ElMessage.error('加载优惠券列表失败: ' + (e.message || '网络错误'))
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', type: 'FULL_REDUCTION', discountValue: 0, minAmount: 0, totalQuantity: 100, validDays: 30, status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入优惠券名称')
    return
  }
  try {
    if (editingId.value) {
      await adminApi.updateCoupon(editingId.value, form)
      ElMessage.success('优惠券已更新')
    } else {
      await adminApi.saveCoupon(form)
      ElMessage.success('优惠券已创建')
    }
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('保存优惠券失败: ' + (e.message || '网络错误'))
  }
}

async function removeRow(id) {
  try {
    await ElMessageBox.confirm('确定要删除该优惠券吗？', '确认删除', { type: 'warning' })
    await adminApi.deleteCoupon(id)
    ElMessage.success('已删除')
    await loadData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除优惠券失败: ' + (e.message || '网络错误'))
    }
  }
}

function openDistribute(row) {
  distributeForm.templateId = row.id
  distributeForm.userIds = ''
  distributeVisible.value = true
}

async function submitDistribute() {
  const ids = distributeForm.userIds.split(',').map(s => parseInt(s.trim(), 10)).filter(n => !isNaN(n))
  if (!ids.length) {
    ElMessage.warning('请输入用户ID（逗号分隔）')
    return
  }
  try {
    await adminApi.distributeCoupon(distributeForm.templateId, { userIds: ids })
    ElMessage.success(`已向 ${ids.length} 个用户分发优惠券`)
    distributeVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('分发优惠券失败: ' + (e.message || '网络错误'))
  }
}

onMounted(loadData)
</script>

<template>
  <div class="admin-table-card">
    <div class="admin-toolbar">
      <h3>优惠券管理</h3>
      <el-button type="primary" @click="openCreate">新增优惠券</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ row.type === 'FULL_REDUCTION' ? '满减' : row.type === 'DISCOUNT' ? '折扣' : row.type }}</template>
      </el-table-column>
      <el-table-column label="优惠值" width="100">
        <template #default="{ row }">
          {{ row.type === 'DISCOUNT' ? row.discountValue + '%' : '¥' + row.discountValue }}
        </template>
      </el-table-column>
      <el-table-column prop="minAmount" label="最低消费" width="90" />
      <el-table-column label="领取/总量" width="100">
        <template #default="{ row }">{{ row.receivedQuantity }} / {{ row.totalQuantity }}</template>
      </el-table-column>
      <el-table-column prop="validDays" label="有效期(天)" width="100" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button text @click="openEdit(row)">编辑</el-button>
          <el-button text type="success" @click="openDistribute(row)">分发</el-button>
          <el-button text type="danger" @click="removeRow(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑优惠券' : '新增优惠券'" width="520px">
      <el-form label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="满减" value="FULL_REDUCTION" />
            <el-option label="折扣（填90=9折）" value="DISCOUNT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="form.type === 'DISCOUNT' ? '折扣率(%)' : '优惠金额'">
          <el-input-number v-model="form.discountValue" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="最低消费"><el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="发行总量"><el-input-number v-model="form.totalQuantity" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="有效天数"><el-input-number v-model="form.validDays" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="distributeVisible" title="分发优惠券" width="440px">
      <el-form label-width="100px">
        <el-form-item label="用户ID"><el-input v-model="distributeForm.userIds" placeholder="输入用户ID，逗号分隔" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="distributeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDistribute">确认分发</el-button>
      </template>
    </el-dialog>
  </div>
</template>
