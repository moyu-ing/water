<script setup>
import { onMounted, ref } from 'vue'
import { deliveryApi, uploadFile } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const tasks = ref([])
const statusMap = { ASSIGNED: '已分配', PICKED_UP: '已取件', DELIVERING: '配送中', COMPLETED: '已完成' }
const statusTypeMap = { ASSIGNED: 'warning', PICKED_UP: '', DELIVERING: 'primary', COMPLETED: 'success' }

async function loadData() {
  try {
    tasks.value = await deliveryApi.tasks()
  } catch (e) {
    ElMessage.error('加载配送任务失败: ' + (e.message || '网络错误'))
  }
}

async function updateStatus(task, newStatus) {
  try {
    await deliveryApi.updateTaskStatus(task.id, { status: newStatus })
    ElMessage.success('状态已更新')
    await loadData()
  } catch (e) {
    ElMessage.error('更新状态失败: ' + (e.message || '网络错误'))
  }
}

async function completeTask(task) {
  try {
    const { value } = await ElMessageBox.prompt('请输入回收的空桶数量', '完成配送', {
      confirmButtonText: '确认签收',
      inputValue: '0',
      inputPattern: /^\d+$/,
      inputErrorMessage: '请输入整数',
    })
    const barrelReturned = parseInt(value || '0', 10)
    await deliveryApi.completeTask(task.id, {
      photoUrl: '',
      remark: '配送完成',
      barrelReturned: barrelReturned,
    })
    ElMessage.success(barrelReturned > 0 ? `已完成签收，回收空桶 ${barrelReturned} 个` : '已完成签收')
    await loadData()
  } catch (e) {
    ElMessage.error('完成配送失败: ' + (e.message || '网络错误'))
  }
}

onMounted(loadData)
</script>

<template>
  <div class="tasks-page">
    <h3>我的配送任务</h3>
    <div v-if="tasks.length" class="task-list">
      <div v-for="task in tasks" :key="task.id" class="soft-card task-card">
        <div class="task-header">
          <span class="task-order-no">📦 {{ task.orderNo }}</span>
          <el-tag :type="statusTypeMap[task.status]" size="small">{{ statusMap[task.status] || task.status }}</el-tag>
        </div>
        <div class="task-info">
          <p><strong>收货人：</strong>{{ task.orderContactName }} {{ task.orderContactPhone }}</p>
          <p><strong>地址：</strong>{{ task.orderFullAddress }}</p>
          <p><strong>金额：</strong>¥ {{ task.orderTotalAmount }}</p>
          <p v-if="task.barrelReturned > 0">🪣 已回收空桶: {{ task.barrelReturned }} 个</p>
        </div>
        <div class="task-actions">
          <el-button v-if="task.status === 'ASSIGNED'" type="primary" @click="updateStatus(task, 'PICKED_UP')">确认取件</el-button>
          <el-button v-if="task.status === 'PICKED_UP'" type="primary" @click="updateStatus(task, 'DELIVERING')">开始配送</el-button>
          <el-button v-if="task.status === 'DELIVERING'" type="success" @click="completeTask(task)">完成签收</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无配送任务" />
  </div>
</template>

<style scoped>
.tasks-page h3 { margin: 0 0 18px; }
.task-list { display: flex; flex-direction: column; gap: 14px; }
.task-card { padding: 18px; }
.task-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.task-order-no { font-weight: 700; font-size: 16px; }
.task-info p { margin: 8px 0; color: var(--muted); }
.task-info strong { color: #333; }
.task-actions { margin-top: 14px; display: flex; gap: 10px; }
</style>
