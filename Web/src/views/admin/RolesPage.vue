<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'

const rows = ref([])
const menus = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({
  name: '',
  code: '',
  description: '',
  status: 1,
  menuIds: [],
})

async function loadData() {
  try {
    const [roleRows, menuRows] = await Promise.all([adminApi.roles(), adminApi.menus()])
    rows.value = roleRows
    menus.value = menuRows
  } catch (e) {
    ElMessage.error('加载角色列表失败: ' + (e.message || '网络错误'))
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', code: '', description: '', status: 1, menuIds: [] })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { name: row.name, code: row.code, description: row.description, status: row.status, menuIds: [...row.menuIds] })
  dialogVisible.value = true
}

async function submit() {
  try {
    if (editingId.value) {
      await adminApi.updateRole(editingId.value, form)
      ElMessage.success('角色已更新')
    } else {
      await adminApi.saveRole(form)
      ElMessage.success('角色已创建')
    }
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('保存角色失败: ' + (e.message || '网络错误'))
  }
}

async function removeRow(id) {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '确认删除', { type: 'warning' })
    await adminApi.deleteRole(id)
    ElMessage.success('角色已删除')
    await loadData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除角色失败: ' + (e.message || '网络错误'))
    }
  }
}

onMounted(loadData)
</script>

<template>
  <div class="admin-table-card">
    <div class="admin-toolbar">
      <h3>角色管理</h3>
      <el-button type="primary" @click="openCreate">新增角色</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="角色名称" />
      <el-table-column prop="code" label="角色编码" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button text @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" @click="removeRow(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="620px">
      <el-form label-width="90px">
        <el-form-item label="角色名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="角色编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="角色说明"><el-input v-model="form.description" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="菜单权限">
          <el-select v-model="form.menuIds" multiple style="width: 100%">
            <el-option v-for="item in menus" :key="item.id" :label="`${item.name}${item.permissionCode ? ` (${item.permissionCode})` : ''}`" :value="item.id" />
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
