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
  image: '',
  sortNum: 0,
  status: 1,
})

async function loadData() {
  rows.value = await adminApi.categories()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { parentId: 0, name: '', image: '', sortNum: 0, status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await adminApi.updateCategory(editingId.value, form)
    ElMessage.success('分类已更新')
  } else {
    await adminApi.saveCategory(form)
    ElMessage.success('分类已创建')
  }
  dialogVisible.value = false
  await loadData()
}

async function removeRow(id) {
  await adminApi.deleteCategory(id)
  ElMessage.success('分类已删除')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="admin-table-card">
    <div class="admin-toolbar">
      <h3>商品分类</h3>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="sortNum" label="排序" />
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="560px">
      <el-form label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="图片"><el-input v-model="form.image" /></el-form-item>
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
