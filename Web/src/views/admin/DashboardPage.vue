<script setup>
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api'

const stats = ref({
  totalUsers: 0,
  totalProducts: 0,
  totalCategories: 0,
  totalOrders: 0,
  todayNewOrders: 0,
  todayRevenue: 0,
  pendingCount: 0,
  toDeliverCount: 0,
  completedCount: 0,
  cancelledCount: 0,
  lowStockProducts: [],
  sevenDayTrend: [],
  topCategories: [],
})

const summaryCards = computed(() => [
  { label: '商城用户', value: stats.value.totalUsers, color: '#0f6cbf' },
  { label: '商品数量', value: stats.value.totalProducts, color: '#14b8a6' },
  { label: '商品分类', value: stats.value.totalCategories, color: '#f59e0b' },
  { label: '订单总数', value: stats.value.totalOrders, color: '#8b5cf6' },
])

const todayCards = computed(() => [
  { label: '今日新订单', value: stats.value.todayNewOrders, icon: '📦' },
  { label: '今日销售额', value: '¥' + Number(stats.value.todayRevenue || 0).toFixed(2), icon: '💰' },
  { label: '待处理订单', value: stats.value.pendingCount, icon: '⏳' },
  { label: '配送中', value: stats.value.toDeliverCount, icon: '🚚' },
])

const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 40, right: 20, top: 20, bottom: 30 },
  xAxis: {
    type: 'category',
    data: stats.value.sevenDayTrend.map(d => d.date?.slice(5) || ''),
    axisLabel: { fontSize: 11 },
  },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    name: '订单数',
    type: 'line',
    data: stats.value.sevenDayTrend.map(d => d.count || 0),
    smooth: true,
    lineStyle: { color: '#0f6cbf', width: 3 },
    itemStyle: { color: '#0f6cbf' },
    areaStyle: { color: 'rgba(15,108,191,0.08)' },
  }],
}))

const statusPieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { fontSize: 11 } },
  series: [{
    name: '订单状态',
    type: 'pie',
    radius: ['45%', '72%'],
    center: ['50%', '45%'],
    label: { show: false },
    emphasis: { label: { show: true } },
    data: [
      { value: stats.value.pendingCount, name: '待确认', itemStyle: { color: '#f59e0b' } },
      { value: stats.value.toDeliverCount, name: '待配送', itemStyle: { color: '#0f6cbf' } },
      { value: stats.value.completedCount, name: '已完成', itemStyle: { color: '#14b8a6' } },
      { value: stats.value.cancelledCount, name: '已取消', itemStyle: { color: '#ef4444' } },
    ].filter(d => d.value > 0),
  }],
}))

const categoryPieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    name: '商品分布',
    type: 'pie',
    radius: '70%',
    center: ['50%', '50%'],
    label: { formatter: '{b}: {c}' },
    data: stats.value.topCategories.map(c => ({
      value: c.count,
      name: c.categoryName,
    })),
  }],
}))

async function loadData() {
  try {
    stats.value = await adminApi.statistics()
  } catch (e) {
    ElMessage.error('加载统计数据失败: ' + (e.message || '网络错误'))
  }
}

onMounted(loadData)
</script>

<template>
  <div class="dashboard-page">
    <!-- 汇总卡片 -->
    <div class="stat-grid">
      <section v-for="card in summaryCards" :key="card.label" class="soft-card stat-card">
        <span>{{ card.label }}</span>
        <strong :style="{ color: card.color }">{{ card.value }}</strong>
      </section>
    </div>

    <!-- 今日指标 -->
    <div class="stat-grid today-grid">
      <section v-for="card in todayCards" :key="card.label" class="soft-card stat-card today-card">
        <span>{{ card.icon }} {{ card.label }}</span>
        <strong>{{ card.value }}</strong>
      </section>
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <section class="soft-card chart-card">
        <h3>近7日订单趋势</h3>
        <v-chart v-if="stats.sevenDayTrend.length" :option="trendChartOption" style="height:280px" autoresize />
        <el-empty v-else description="暂无数据" />
      </section>
      <section class="soft-card chart-card">
        <h3>订单状态分布</h3>
        <v-chart v-if="stats.pendingCount + stats.completedCount > 0" :option="statusPieOption" style="height:280px" autoresize />
        <el-empty v-else description="暂无订单数据" />
      </section>
    </div>

    <div class="chart-row">
      <section class="soft-card chart-card">
        <h3>商品分类 TOP5</h3>
        <v-chart v-if="stats.topCategories.length" :option="categoryPieOption" style="height:260px" autoresize />
        <el-empty v-else description="暂无商品数据" />
      </section>
      <section class="soft-card chart-card">
        <h3>库存预警</h3>
        <el-table v-if="stats.lowStockProducts?.length" :data="stats.lowStockProducts" stripe size="small">
          <el-table-column prop="name" label="商品名称" />
          <el-table-column prop="spec" label="规格" width="110" />
          <el-table-column prop="stock" label="当前库存" width="90">
            <template #default="{ row }">
              <el-tag type="danger" size="small">{{ row.stock }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="80" />
        </el-table>
        <el-empty v-else description="暂无库存预警商品" />
      </section>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-card span {
  color: var(--muted);
  font-size: 14px;
}

.stat-card strong {
  font-size: 36px;
}

.today-card strong {
  font-size: 26px;
  color: var(--brand);
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-card {
  padding: 20px;
}

.chart-card h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-row {
    grid-template-columns: 1fr;
  }
}
</style>
