<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminApi, uploadFile } from '../../api'

const rows = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const uploadUrl = ref('')
const uploadHeaders = ref({})
const form = reactive({
  categoryId: null,
  name: '',
  subTitle: '',
  image: '',
  description: '',
  spec: '',
  price: 0,
  stock: 0,
  barrelDeposit: 0,
  isBarrel: 0,
  status: 1,
})

async function loadData() {
  try {
    const [productRows, categoryRows] = await Promise.all([adminApi.products(), adminApi.categories()])
    rows.value = productRows
    categories.value = categoryRows
  } catch (e) {
    ElMessage.error('加载商品列表失败: ' + (e.message || '网络错误'))
  }
}

const imageUploadRef = ref(null)

function openCreate() {
  editingId.value = null
  Object.assign(form, { categoryId: null, name: '', subTitle: '', image: '', description: '', spec: '', price: 0, stock: 0, barrelDeposit: 0, isBarrel: 0, status: 1 })
  imageUploadRef.value?.clearFiles()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function handleImageUpload(file) {
  try {
    const urls = await uploadFile(file)
    if (urls && urls.length > 0) {
      form.image = urls[0]
      ElMessage.success('图片上传成功')
    }
  } catch (e) {
    ElMessage.error('图片上传失败: ' + (e.message || '未知错误'))
  }
  return false
}

function beforeImageUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择商品分类')
    return
  }
  if (form.price <= 0) {
    ElMessage.warning('请输入商品价格')
    return
  }
  try {
    if (editingId.value) {
      await adminApi.updateProduct(editingId.value, form)
      ElMessage.success('商品已更新')
    } else {
      await adminApi.saveProduct(form)
      ElMessage.success('商品已创建')
    }
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('保存商品失败: ' + (e.message || '网络错误'))
  }
}

async function removeRow(id) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '确认删除', { type: 'warning' })
    await adminApi.deleteProduct(id)
    ElMessage.success('商品已删除')
    await loadData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除商品失败: ' + (e.message || '网络错误'))
    }
  }
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
        <el-form-item label="图片">
          <div style="display:flex;align-items:center;gap:12px;">
            <el-upload
              ref="imageUploadRef"
              :http-request="({ file }) => handleImageUpload(file)"
              :before-upload="beforeImageUpload"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary" plain>选择图片上传</el-button>
            </el-upload>
            <el-input v-model="form.image" placeholder="或手动输入图片URL" style="flex:1" />
          </div>
          <img v-if="form.image" :src="form.image" style="width:120px;height:80px;object-fit:cover;border-radius:8px;margin-top:8px;" />
        </el-form-item>
        <el-form-item label="规格"><el-input v-model="form.spec" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="桶押金"><el-input-number v-model="form.barrelDeposit" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="是否含桶"><el-switch v-model="form.isBarrel" :active-value="1" :inactive-value="0" /></el-form-item>
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
