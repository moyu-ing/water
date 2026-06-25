<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, uploadFile } from '../../api'

const rows = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const imageUploadRef = ref(null)
const form = reactive({
  parentId: 0,
  name: '',
  image: '',
  sortNum: 0,
  status: 1,
})

async function loadData() {
  try {
    rows.value = await adminApi.categories()
  } catch (e) {
    ElMessage.error('加载分类列表失败: ' + (e.message || '网络错误'))
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { parentId: 0, name: '', image: '', sortNum: 0, status: 1 })
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
  try {
    if (editingId.value) {
      await adminApi.updateCategory(editingId.value, form)
      ElMessage.success('分类已更新')
    } else {
      await adminApi.saveCategory(form)
      ElMessage.success('分类已创建')
    }
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('保存分类失败: ' + (e.message || '网络错误'))
  }
}

async function removeRow(id) {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '确认删除', { type: 'warning' })
    await adminApi.deleteCategory(id)
    ElMessage.success('分类已删除')
    await loadData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除分类失败: ' + (e.message || '网络错误'))
    }
  }
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
