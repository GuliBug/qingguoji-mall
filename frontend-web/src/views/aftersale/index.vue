<template>
  <div class="page-wrapper">
    <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true" class="search-card">
      <el-form-item label="售后单号" prop="afterNo">
        <el-input
          v-model="queryParams.afterNo"
          placeholder="请输入售后单号"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="售后状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option v-for="item in AFTERSALE_STATUS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="售后类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 150px">
          <el-option v-for="item in AFTERSALE_TYPE" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">售后列表</div>
        <RightToolbar v-model:show-search="showSearch" @query-table="getList" />
      </div>

      <el-table v-loading="loading" :data="pagedList" border>
        <el-table-column label="售后单号" prop="afterNo" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="110" align="center">
          <template #default="scope">
            <DictTag :options="AFTERSALE_TYPE" :value="scope.row.type" />
          </template>
        </el-table-column>
        <el-table-column label="订单ID" prop="orderId" width="90" align="center" />
        <el-table-column label="会员ID" prop="memberId" width="90" align="center" />
        <el-table-column label="数量" prop="qty" width="80" align="center" />
        <el-table-column label="退款金额" width="110" align="center">
          <template #default="scope">
            <span class="amount">{{ formatMoney(scope.row.refundAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <DictTag :options="AFTERSALE_STATUS" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="退货物流" width="150" align="center">
          <template #default="scope">
            <span :class="scope.row.returnLogisticsNo ? '' : 'text-muted'">
              {{ scope.row.returnLogisticsNo || '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="170" align="center">
          <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status === 'WAIT_AUDIT'">
              <el-button link type="primary" @click="handleAudit(scope.row, true)">通过</el-button>
              <el-button link type="danger" @click="handleAudit(scope.row, false)">拒绝</el-button>
            </template>
            <template v-else-if="scope.row.status === 'WAIT_RECEIVE'">
              <el-button link type="primary" @click="handleConfirmReturn(scope.row)">确认收货并退款</el-button>
            </template>
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

    <el-dialog v-model="open" :title="auditPass ? '售后审核' : '拒绝售后'" width="440px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="售后单号">
          <span>{{ currentRow ? currentRow.afterNo : '' }}</span>
        </el-form-item>
        <el-form-item label="退款金额">
          <span class="amount">{{ currentRow ? formatMoney(currentRow.refundAmount) : '' }}</span>
        </el-form-item>
        <el-form-item v-if="!auditPass" label="拒绝原因" prop="rejectReason">
          <el-input
            v-model="form.rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请输入拒绝原因"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-else label="审核意见">
          <span class="text-muted">确认通过该售后申请？通过后将进入下一步流程。</span>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { listAfterSale, auditAfterSale, confirmReturn } from '@/api/aftersale'
import { AFTERSALE_STATUS, AFTERSALE_TYPE } from '@/utils/dict'
import { formatMoney, formatDateTime } from '@/utils/format'

defineOptions({ name: 'AfterSale' })

const loading = ref(false)
const submitting = ref(false)
const showSearch = ref(true)
const open = ref(false)
const auditPass = ref(true)
const afterSaleList = ref([])
const currentRow = ref(null)
const formRef = ref(null)
const queryFormRef = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  afterNo: '',
  status: '',
  type: ''
})

const form = ref({
  rejectReason: ''
})

const rules = computed(() => {
  if (auditPass.value) {
    return {}
  }
  return {
    rejectReason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
  }
})

// 后端 /aftersales 支持 status 与分页，不支持单号/类型查询，故本地过滤分页
const filteredList = computed(() => {
  const { afterNo, status, type } = queryParams.value
  return afterSaleList.value.filter((item) => {
    if (afterNo && !(item.afterNo || '').includes(afterNo)) return false
    if (status && item.status !== status) return false
    if (type && item.type !== type) return false
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
 * 查询售后列表
 */
function getList() {
  loading.value = true
  listAfterSale({ pageNum: 1, pageSize: 500 })
    .then((res) => {
      afterSaleList.value = res.rows
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
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, afterNo: '', status: '', type: '' }
  getList()
}

/**
 * 打开审核弹窗
 * @param {object} row
 * @param {boolean} pass 是否通过
 */
function handleAudit(row, pass) {
  currentRow.value = row
  auditPass.value = pass
  form.value = { rejectReason: '' }
  open.value = true
}

/**
 * 提交审核
 */
function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) {
      return
    }
    submitting.value = true
    auditAfterSale(currentRow.value.id, {
      pass: auditPass.value,
      rejectReason: auditPass.value ? null : form.value.rejectReason
    })
      .then(() => {
        ElMessage.success(auditPass.value ? '审核通过' : '已拒绝')
        open.value = false
        getList()
      })
      .finally(() => {
        submitting.value = false
      })
  })
}

/**
 * 确认收到退货并退款
 * @param {object} row
 */
function handleConfirmReturn(row) {
  ElMessageBox.confirm(`确认已收到售后单「${row.afterNo}」寄回的商品吗？确认后将执行退款并回补库存。`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => confirmReturn(row.id))
    .then(() => {
      ElMessage.success('确认收货成功，退款已完成')
      getList()
    })
    .catch(() => {})
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
</style>
