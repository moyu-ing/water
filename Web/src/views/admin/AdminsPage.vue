<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api'

const rows = ref([])
const roles = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({
  username: '',
  password: '',
  nickname: '',
  status: 1,
  roleIds: [],
})

async function loadData() {
  const [adminUsers, roleRows] = await Promise.all([adminApi.adminUsers(), adminApi.roles()])
  rows.value = adminUsers
  roles.value = roleRows
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { username: '', password: '', nickname: '', status: 1, roleIds: [] })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { username: row.username, password: '', nickname: row.nickname, status: row.status, roleIds: [...row.roleIds] })
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await adminApi.updateAdminUser(editingId.value, form)
    ElMessage.success('管理员已更新')
  } else {
    await adminApi.saveAdminUser(form)
    ElMessage.success('管理员已创建')
  }
  dialogVisible.value = false
  await loadData()
}

async function removeRow(id) {
  await adminApi.deleteAdminUser(id)
  ElMessage.success('管理员已删除')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="admin-table-card">
    <div class="admin-toolbar">
      <h3>管理员管理</h3>
      <el-button type="primary" @click="openCreate">新增管理员</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column label="角色">
        <template #default="{ row }">{{ row.roleNames?.join('、') }}</template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑管理员' : '新增管理员'" width="560px">
      <el-form label-width="90px">
        <el-form-item label="账号"><el-input v-model="form.username" :disabled="!!editingId" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple style="width: 100%">
            <el-option v-for="item in roles" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
