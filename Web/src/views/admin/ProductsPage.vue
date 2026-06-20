<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api'

const rows = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({
  categoryId: null,
  name: '',
  subTitle: '',
  image: '',
  description: '',
  spec: '',
  price: 0,
  stock: 0,
  status: 1,
})

async function loadData() {
  const [productRows, categoryRows] = await Promise.all([adminApi.products(), adminApi.categories()])
  rows.value = productRows
  categories.value = categoryRows
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { categoryId: null, name: '', subTitle: '', image: '', description: '', spec: '', price: 0, stock: 0, status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await adminApi.updateProduct(editingId.value, form)
    ElMessage.success('商品已更新')
  } else {
    await adminApi.saveProduct(form)
    ElMessage.success('商品已创建')
  }
  dialogVisible.value = false
  await loadData()
}

async function removeRow(id) {
  await adminApi.deleteProduct(id)
  ElMessage.success('商品已删除')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="admin-table-card">
    <div class="admin-toolbar">
      <h3>商品管理</h3>
      <el-button type="primary" @click="openCreate">新增商品</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商品名称" min-width="220" />
      <el-table-column prop="categoryName" label="分类" />
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="stock" label="库存" />
      <el-table-column label="状态">
        <template #default="{ row }">{{ row.status === 1 ? '上架' : '下架' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button text @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" @click="removeRow(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑商品' : '新增商品'" width="680px">
      <el-form label-width="92px">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subTitle" /></el-form-item>
        <el-form-item label="图片"><el-input v-model="form.image" /></el-form-item>
        <el-form-item label="规格"><el-input v-model="form.spec" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
