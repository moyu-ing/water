<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api'

const rows = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({
  parentId: 0,
  name: '',
  path: '',
  component: '',
  permissionCode: '',
  type: 1,
  sortNum: 0,
  status: 1,
})

async function loadData() {
  rows.value = await adminApi.menus()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { parentId: 0, name: '', path: '', component: '', permissionCode: '', type: 1, sortNum: 0, status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await adminApi.updateMenu(editingId.value, form)
    ElMessage.success('菜单已更新')
  } else {
    await adminApi.saveMenu(form)
    ElMessage.success('菜单已创建')
  }
  dialogVisible.value = false
  await loadData()
}

async function removeRow(id) {
  await adminApi.deleteMenu(id)
  ElMessage.success('菜单已删除')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="admin-table-card">
    <div class="admin-toolbar">
      <h3>菜单与权限</h3>
      <el-button type="primary" @click="openCreate">新增菜单</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="path" label="路由" />
      <el-table-column prop="permissionCode" label="权限码" />
      <el-table-column prop="type" label="类型" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button text @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" @click="removeRow(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑菜单' : '新增菜单'" width="620px">
      <el-form label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="父级ID"><el-input-number v-model="form.parentId" :min="0" /></el-form-item>
        <el-form-item label="路由"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="组件"><el-input v-model="form.component" /></el-form-item>
        <el-form-item label="权限码"><el-input v-model="form.permissionCode" /></el-form-item>
        <el-form-item label="类型"><el-input-number v-model="form.type" :min="1" :max="2" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortNum" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
