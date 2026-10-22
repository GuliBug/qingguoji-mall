<template>
  <div class="page-wrapper">
    <el-row :gutter="16" class="stat-row">
      <el-col v-for="card in cards" :key="card.key" :xs="12" :sm="8" :md="8" :lg="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="22"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ card.label }}</div>
              <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button link type="primary" :icon="Refresh" :loading="loading" @click="getList">刷新</el-button>
            </div>
          </template>
          <el-table v-loading="loading" :data="todos" border>
            <el-table-column prop="label" label="事项" min-width="140" />
            <el-table-column prop="count" label="数量" width="100" align="center">
              <template #default="scope">
                <span :class="scope.row.count > 0 ? 'amount' : 'text-muted'">{{ scope.row.count }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="scope">
                <el-button link type="primary" @click="goto(scope.row.path)">去处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <span>系统信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="项目名称">青果纪 · 槟榔品牌会员商城</el-descriptions-item>
            <el-descriptions-item label="后端服务">Spring Boot 3.5 + JPA + H2（内存库）</el-descriptions-item>
            <el-descriptions-item label="前端工程">Vue 3 + Element Plus + Vite（若依风格）</el-descriptions-item>
            <el-descriptions-item label="数据说明">
              种子数据：10 个商品 / 30 个防伪码；订单与会员数据随客户端下单产生
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh, Money, Tickets, Van, Warning, User, Goods } from '@element-plus/icons-vue'
import { getDashboard } from '@/api/dashboard'
import { formatMoney } from '@/utils/format'

defineOptions({ name: 'Dashboard' })

const router = useRouter()
const loading = ref(false)
const data = ref({})
let timer = null

const cards = computed(() => [
  {
    key: 'todaySales',
    label: '今日销售额',
    value: formatMoney(data.value.todaySales || 0),
    icon: Money,
    color: '#409eff',
    bg: '#ecf5ff'
  },
  {
    key: 'waitShipOrders',
    label: '待发货订单',
    value: data.value.waitShipOrders || 0,
    icon: Van,
    color: '#e6a23c',
    bg: '#fdf6ec'
  },
  {
    key: 'waitAuditAfterSales',
    label: '待审核售后',
    value: data.value.waitAuditAfterSales || 0,
    icon: Warning,
    color: '#f56c6c',
    bg: '#fef0f0'
  },
  {
    key: 'memberCount',
    label: '会员总数',
    value: data.value.memberCount || 0,
    icon: User,
    color: '#67c23a',
    bg: '#f0f9eb'
  },
  {
    key: 'lowStockSkuCount',
    label: '低库存 SKU',
    value: data.value.lowStockSkuCount || 0,
    icon: Goods,
    color: '#f56c6c',
    bg: '#fef0f0'
  },
  {
    key: 'todo',
    label: '待办合计',
    value: (data.value.waitShipOrders || 0) + (data.value.waitAuditAfterSales || 0),
    icon: Tickets,
    color: '#909399',
    bg: '#f4f4f5'
  }
])

const todos = computed(() => [
  { label: '待发货订单', count: data.value.waitShipOrders || 0, path: '/order' },
  { label: '待审核售后', count: data.value.waitAuditAfterSales || 0, path: '/aftersale' },
  { label: '低库存 SKU（可售 < 10）', count: data.value.lowStockSkuCount || 0, path: '/stock' }
])

/**
 * 查询看板数据
 */
function getList() {
  loading.value = true
  getDashboard()
    .then((res) => {
      data.value = res.data || {}
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * 跳转到对应模块
 * @param {string} path
 */
function goto(path) {
  router.push(path)
}

onMounted(() => {
  getList()
  // 与旧版一致：30 秒自动刷新
  timer = setInterval(getList, 30000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.stat-row {
  margin-bottom: 4px;
}

.stat-card {
  margin-bottom: 16px;
}

.stat-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  line-height: 1;
}

.panel-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
