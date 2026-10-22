<template>
  <div class="page-wrapper">
    <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true" class="search-card">
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="会员等级" prop="level">
        <el-select v-model="queryParams.level" placeholder="全部等级" clearable style="width: 150px">
          <el-option v-for="item in MEMBER_LEVEL" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">会员列表</div>
        <RightToolbar v-model:show-search="showSearch" @query-table="getList" />
      </div>

      <el-table v-loading="loading" :data="pagedList" border>
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column label="手机号" prop="phone" min-width="140" />
        <el-table-column label="昵称" min-width="130">
          <template #default="scope">{{ scope.row.nickname || '—' }}</template>
        </el-table-column>
        <el-table-column label="等级" width="110" align="center">
          <template #default="scope">
            <DictTag :options="MEMBER_LEVEL" :value="scope.row.level" />
          </template>
        </el-table-column>
        <el-table-column label="积分" width="100" align="center">
          <template #default="scope">
            <span class="amount">{{ scope.row.points ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成长值" prop="growth" width="110" align="center" />
        <el-table-column label="注册时间" width="170" align="center">
          <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" :icon="Edit" @click="handleAdjustPoints(scope.row)">调整积分</el-button>
            <el-button link type="primary" :icon="Tickets" @click="handlePointsLog(scope.row)">流水</el-button>
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

    <!-- 积分调整 -->
    <el-dialog v-model="open" title="调整积分" width="440px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="会员">
          <span>{{ currentRow ? currentRow.phone : '' }}</span>
        </el-form-item>
        <el-form-item label="当前积分">
          <span class="amount">{{ currentRow ? currentRow.points : 0 }}</span>
        </el-form-item>
        <el-form-item label="变动积分" prop="delta">
          <el-input-number v-model="form.delta" :min="-999999" :max="999999" :step="1" style="width: 100%" />
          <div class="form-tip">正数发放、负数扣减，不能为 0</div>
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="form.reason" placeholder="请输入调整原因" maxlength="50" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 积分流水 -->
    <el-dialog v-model="logOpen" title="积分流水" width="720px" append-to-body>
      <el-table v-loading="logLoading" :data="logList" border size="small">
        <el-table-column label="时间" width="170" align="center">
          <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="120" align="center">
          <template #default="scope">
            <DictTag :options="POINTS_TYPE" :value="scope.row.type" />
          </template>
        </el-table-column>
        <el-table-column label="变动" width="100" align="center">
          <template #default="scope">
            <span :class="scope.row.amount > 0 ? 'amount' : 'text-muted'">
              {{ scope.row.amount > 0 ? '+' : '' }}{{ scope.row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="200" show-overflow-tooltip />
      </el-table>
      <pagination
        v-show="logTotal > 0"
        v-model:page="logQuery.pageNum"
        v-model:limit="logQuery.pageSize"
        :total="logTotal"
        @pagination="getLogList"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Edit, Tickets } from '@element-plus/icons-vue'
import { listMember, adjustPoints, listPointsLog } from '@/api/member'
import { MEMBER_LEVEL, POINTS_TYPE } from '@/utils/dict'
import { formatDateTime } from '@/utils/format'

defineOptions({ name: 'Member' })

const loading = ref(false)
const submitting = ref(false)
const showSearch = ref(true)
const open = ref(false)
const logOpen = ref(false)
const logLoading = ref(false)
const memberList = ref([])
const logList = ref([])
const logTotal = ref(0)
const currentRow = ref(null)
const formRef = ref(null)
const queryFormRef = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  phone: '',
  level: ''
})

const logQuery = ref({
  pageNum: 1,
  pageSize: 10
})

const form = ref({
  delta: null,
  reason: '管理端调整'
})

const rules = {
  delta: [
    { required: true, message: '请输入变动积分', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('调整积分不能为 0'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

// 后端 /members 不支持手机号/等级查询，本地过滤分页
const filteredList = computed(() => {
  const { phone, level } = queryParams.value
  return memberList.value.filter((item) => {
    if (phone && !(item.phone || '').includes(phone)) return false
    if (level !== '' && level !== null && level !== undefined && item.level !== level) return false
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
 * 查询会员列表
 */
function getList() {
  loading.value = true
  listMember({ pageNum: 1, pageSize: 500 })
    .then((res) => {
      memberList.value = res.rows
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
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, phone: '', level: '' }
  getList()
}

/**
 * 打开积分调整弹窗
 * @param {object} row
 */
function handleAdjustPoints(row) {
  currentRow.value = row
  form.value = { delta: null, reason: '管理端调整' }
  open.value = true
}

/**
 * 提交积分调整
 */
function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) {
      return
    }
    submitting.value = true
    adjustPoints(currentRow.value.id, { delta: form.value.delta, reason: form.value.reason })
      .then(() => {
        ElMessage.success('积分调整成功')
        open.value = false
        getList()
      })
      .finally(() => {
        submitting.value = false
      })
  })
}

/**
 * 打开积分流水
 * @param {object} row
 */
function handlePointsLog(row) {
  currentRow.value = row
  logQuery.value = { pageNum: 1, pageSize: 10 }
  logOpen.value = true
  getLogList()
}

/**
 * 查询积分流水
 */
function getLogList() {
  if (!currentRow.value) {
    return
  }
  logLoading.value = true
  listPointsLog(currentRow.value.id, logQuery.value)
    .then((res) => {
      logList.value = res.rows
      logTotal.value = res.total
    })
    .finally(() => {
      logLoading.value = false
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

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
</style>
