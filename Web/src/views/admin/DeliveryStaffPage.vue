<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'

const rows = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({ username: '', password: '', name: '', phone: '', status: 1 })

async function loadData() {
  try {
    rows.value = await adminApi.deliveryStaff()
  } catch (e) {
    ElMessage.error('加载配送员列表失败: ' + (e.message || '网络错误'))
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { username: '', password: '', name: '', phone: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { username: row.username, password: '', name: row.name, phone: row.phone, status: row.status })
  dialogVisible.value = true
}

async function submit() {
  try {
    if (editingId.value) {
      await adminApi.updateDeliveryStaff(editingId.value, form)
      ElMessage.success('配送员已更新')
    } else {
      await adminApi.saveDeliveryStaff(form)
      ElMessage.success('配送员已创建')
    }
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('保存配送员失败: ' + (e.message || '网络错误'))
  }
}

async function removeRow(id) {
  try {
    await ElMessageBox.confirm('确定要删除该配送员吗？', '确认删除', { type: 'warning' })
    await adminApi.deleteDeliveryStaff(id)
    ElMessage.success('已删除')
    await loadData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除配送员失败: ' + (e.message || '网络错误'))
    }
  }
}

onMounted(loadData)
</script>

<template>
  <div class="admin-table-card">
    <div class="admin-toolbar">
      <h3>配送人员管理</h3>
      <el-button type="primary" @click="openCreate">新增配送员</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column label="状态">
        <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button text @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" @click="removeRow(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑配送员' : '新增配送员'" width="500px">
      <el-form label-width="80px">
        <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" show-password :placeholder="editingId ? '留空不修改密码' : '请输入密码'" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
