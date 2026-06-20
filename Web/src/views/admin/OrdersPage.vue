<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api'

const rows = ref([])

async function loadData() {
  rows.value = await adminApi.orders()
}

async function updateStatus(row, status) {
  await adminApi.updateOrderStatus(row.id, { status })
  ElMessage.success('订单状态已更新')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="admin-table-card">
    <div class="section-title">
      <h3>订单管理</h3>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="orderNo" label="订单号" min-width="220" />
      <el-table-column prop="contactName" label="收货人" />
      <el-table-column prop="contactPhone" label="联系电话" />
      <el-table-column prop="totalAmount" label="金额" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="更新状态" width="260">
        <template #default="{ row }">
          <el-select placeholder="选择状态" style="width: 180px" @change="(value) => updateStatus(row, value)">
            <el-option label="待确认" value="PENDING_CONFIRM" />
            <el-option label="待配送" value="TO_DELIVER" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
