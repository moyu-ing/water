<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api'

const rows = ref([])

async function loadData() {
  try {
    rows.value = await adminApi.users()
  } catch (e) {
    ElMessage.error('加载用户列表失败: ' + (e.message || '网络错误'))
  }
}

onMounted(loadData)
</script>

<template>
  <div class="admin-table-card">
    <div class="section-title">
      <h3>商城用户</h3>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>
