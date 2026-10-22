<template>
  <div class="page-wrapper">
    <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true" class="search-card">
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option v-for="item in ORDER_STATUS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="收货人" prop="receiver">
        <el-input
          v-model="queryParams.receiver"
          placeholder="请输入收货人"
          clearable
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">订单列表</div>
        <RightToolbar v-model:show-search="showSearch" @query-table="getList" />
      </div>

      <el-table v-loading="loading" :data="pagedList" border>
        <el-table-column type="expand">
          <template #default="scope">
            <div class="order-detail">
              <div class="detail-block">
                <div class="detail-title">收货信息</div>
                <div class="detail-line">
                  <span>收货人：{{ scope.row.receiver || '—' }}</span>
                  <span>电话：{{ scope.row.receiverPhone || '—' }}</span>
                </div>
                <div class="detail-line">地址：{{ scope.row.address || '—' }}</div>
              </div>

              <div class="detail-block">
                <div class="detail-title">商品明细</div>
                <el-table :data="scope.row.items || []" size="small" border>
                  <el-table-column label="商品" prop="productName" min-width="160" />
                  <el-table-column label="规格" prop="spec" width="120" align="center" />
                  <el-table-column label="单价" width="100" align="center">
                    <template #default="s">{{ formatMoney(s.row.price) }}</template>
                  </el-table-column>
                  <el-table-column label="数量" prop="qty" width="80" align="center" />
                  <el-table-column label="小计" width="110" align="center">
                    <template #default="s">{{ formatMoney(s.row.payAmount) }}</template>
                  </el-table-column>
                  <el-table-column label="已售后数量" prop="afterSaleQty" width="110" align="center" />
                </el-table>
              </div>

              <div class="detail-block">
                <div class="detail-title">金额与积分</div>
                <div class="detail-line">
                  <span>商品金额：{{ formatMoney(scope.row.goodsAmount) }}</span>
                  <span>优惠：{{ formatMoney(scope.row.discountAmount) }}</span>
                  <span>运费：{{ formatMoney(scope.row.freight) }}</span>
                  <span class="amount">实付：{{ formatMoney(scope.row.payAmount) }}</span>
                </div>
                <div class="detail-line">
                  <span>获得积分：{{ scope.row.pointsEarned ?? 0 }}</span>
                  <span>获得成长值：{{ scope.row.growthEarned ?? 0 }}</span>
                  <span>支付截止：{{ formatDateTime(scope.row.payDeadline) }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="订单号" prop="orderNo" min-width="180" show-overflow-tooltip />
        <el-table-column label="会员ID" prop="memberId" width="90" align="center" />
        <el-table-column label="实付金额" width="120" align="center">
          <template #default="scope">
            <span class="amount">{{ formatMoney(scope.row.payAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <DictTag :options="ORDER_STATUS" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="物流单号" width="170" align="center">
          <template #default="scope">
            <span :class="scope.row.logisticsNo ? '' : 'text-muted'">{{ scope.row.logisticsNo || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="170" align="center">
          <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PAID'"
              link
              type="primary"
              :icon="Van"
              @click="handleShip(scope.row)"
            >
              发货
            </el-button>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
      />
    </div>

    <el-dialog v-model="open" title="订单发货" width="440px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="订单号">
          <span>{{ currentRow ? currentRow.orderNo : '' }}</span>
        </el-form-item>
        <el-form-item label="物流单号" prop="logisticsNo">
          <el-input v-model="form.logisticsNo" placeholder="请输入物流单号" maxlength="40" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Van } from '@element-plus/icons-vue'
import { listOrder, shipOrder } from '@/api/order'
import { ORDER_STATUS } from '@/utils/dict'
import { formatMoney, formatDateTime } from '@/utils/format'

defineOptions({ name: 'Order' })

const loading = ref(false)
const submitting = ref(false)
const showSearch = ref(true)
const open = ref(false)
const orderList = ref([])
const currentRow = ref(null)
const formRef = ref(null)
const queryFormRef = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  status: '',
  receiver: ''
})

const form = ref({
  logisticsNo: ''
})

const rules = {
  logisticsNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}

// 后端 /orders 支持 status 与分页，但不支持订单号/收货人模糊查询，故拉全量后本地过滤
const filteredList = computed(() => {
  const { orderNo, status, receiver } = queryParams.value
  return orderList.value.filter((item) => {
    if (orderNo && !(item.orderNo || '').includes(orderNo)) return false
    if (status && item.status !== status) return false
    if (receiver && !(item.receiver || '').includes(receiver)) return false
    return true
  })
})

const total = computed(() => filteredList.value.length)

const pagedList = computed(() => {
  const { pageNum, pageSize } = queryParams.value
  const start = (pageNum - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
})

/**
 * 查询订单列表
 */
function getList() {
  loading.value = true
  listOrder({ pageNum: 1, pageSize: 500 })
    .then((res) => {
      orderList.value = res.rows
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * 搜索
 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/**
 * 重置
 */
function resetQuery() {
  queryFormRef.value && queryFormRef.value.resetFields()
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, orderNo: '', status: '', receiver: '' }
  getList()
}

/**
 * 打开发货弹窗
 * @param {object} row
 */
function handleShip(row) {
  currentRow.value = row
  form.value = { logisticsNo: '' }
  open.value = true
}

/**
 * 提交发货
 */
function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) {
      return
    }
    submitting.value = true
    shipOrder(currentRow.value.orderNo, form.value.logisticsNo)
      .then(() => {
        ElMessage.success('发货成功')
        open.value = false
        getList()
      })
      .finally(() => {
        submitting.value = false
      })
  })
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.order-detail {
  padding: 8px 16px 16px;
  background: #fafafa;
}

.detail-block {
  margin-bottom: 14px;

  &:last-child {
    margin-bottom: 0;
  }
}

.detail-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  font-size: 13px;
}

.detail-line {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  font-size: 13px;
  color: #606266;
  line-height: 1.9;
}
</style>
