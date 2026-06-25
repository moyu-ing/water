<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api'

const tasks = ref([])
const staffList = ref([])
const dialogVisible = ref(false)
const form = reactive({ orderId: null, staffId: null, pickupAddress: '', deliveryAddress: '', estimatedTime: '' })
const statusMap = { ASSIGNED: '已分配', PICKED_UP: '已取件', DELIVERING: '配送中', COMPLETED: '已完成' }
const statusTypeMap = { ASSIGNED: 'warning', PICKED_UP: '', DELIVERING: 'primary', COMPLETED: 'success' }

async function loadData() {
  try {
    const [t, s] = await Promise.all([adminApi.deliveryTasks(), adminApi.deliveryStaff()])
    tasks.value = t
    staffList.value = s
  } catch (e) {
    ElMessage.error('加载配送任务失败: ' + (e.message || '网络错误'))
  }
}

function openAssign(task) {
  if (task) {
    form.orderId = task.orderId
    form.deliveryAddress = task.orderFullAddress || ''
  } else {
    Object.assign(form, { orderId: null, staffId: null, pickupAddress: '', deliveryAddress: '', estimatedTime: '' })
  }
  dialogVisible.value = true
}

async function submitAssign() {
  try {
    await adminApi.assignDeliveryTask(form)
    ElMessage.success('任务已分配')
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('分配任务失败: ' + (e.message || '网络错误'))
  }
}

onMounted(loadData)
</script>

<template>
  <div class="admin-table-card">
    <div class="admin-toolbar">
      <h3>配送任务管理</h3>
      <el-button type="primary" @click="openAssign()">分配任务</el-button>
    </div>
    <el-table :data="tasks" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单编号" width="220" />
      <el-table-column prop="staffName" label="配送员" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.staffName" size="small">{{ row.staffName }}</el-tag>
          <span v-else style="color:#999">未分配</span>
        </template>
      </el-table-column>
      <el-table-column prop="orderFullAddress" label="配送地址" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTypeMap[row.status]" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="barrelReturned" label="回收空桶" width="90" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 'COMPLETED'" text type="success" disabled>已完成</el-button>
          <el-button v-else text type="primary" @click="openAssign(row)">重新分配</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="分配配送任务" width="560px">
      <el-form label-width="90px">
        <el-form-item label="订单ID"><el-input-number v-model="form.orderId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="配送员">
          <el-select v-model="form.staffId" style="width:100%">
            <el-option v-for="s in staffList" :key="s.id" :label="`${s.name} (${s.phone})`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="取件地址"><el-input v-model="form.pickupAddress" placeholder="仓库地址" /></el-form-item>
        <el-form-item label="配送地址"><el-input v-model="form.deliveryAddress" /></el-form-item>
        <el-form-item label="预计时间">
          <el-date-picker
            v-model="form.estimatedTime"
            type="datetime"
            placeholder="选择预计送达时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>
