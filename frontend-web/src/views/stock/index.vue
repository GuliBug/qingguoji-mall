<template>
  <div class="page-wrapper">
    <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true" class="search-card">
      <el-form-item label="商品" prop="productId">
        <el-select v-model="queryParams.productId" placeholder="全部商品" clearable style="width: 220px">
          <el-option v-for="p in productOptions" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="规格 / 编码" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入规格或 SKU 编码"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="库存预警">
        <el-switch v-model="onlyLowStock" active-text="仅看低库存" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">SKU 库存列表</div>
        <RightToolbar v-model:show-search="showSearch" @query-table="getList" />
      </div>

      <el-table v-loading="loading" :data="pagedList" border>
        <el-table-column label="SKU ID" prop="id" width="80" align="center" />
        <el-table-column label="所属商品" min-width="180" show-overflow-tooltip>
          <template #default="scope">{{ productName(scope.row.productId) }}</template>
        </el-table-column>
        <el-table-column label="规格" prop="spec" width="130" align="center" />
        <el-table-column label="SKU 编码" prop="skuCode" min-width="150" show-overflow-tooltip />
        <el-table-column label="单价" width="110" align="center">
          <template #default="scope">{{ formatMoney(scope.row.price) }}</template>
        </el-table-column>
        <el-table-column label="总库存" prop="totalStock" width="100" align="center" />
        <el-table-column label="锁定库存" prop="lockedStock" width="100" align="center" />
        <el-table-column label="可售库存" width="110" align="center">
          <template #default="scope">
            <el-tag :type="available(scope.row) < 10 ? 'danger' : 'success'" effect="plain">
              {{ available(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" :icon="Edit" @click="handleAdjust(scope.row)">调整库存</el-button>
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

    <el-dialog v-model="open" title="调整库存" width="440px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="SKU">
          <span>{{ currentRow ? productName(currentRow.productId) + ' / ' + currentRow.spec : '' }}</span>
        </el-form-item>
        <el-form-item label="当前可售">
          <span class="amount">{{ currentRow ? available(currentRow) : 0 }}</span>
        </el-form-item>
        <el-form-item label="变动数量" prop="delta">
          <el-input-number v-model="form.delta" :min="-999999" :max="999999" :step="1" style="width: 100%" />
          <div class="form-tip">正数入库、负数出库，不能为 0</div>
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
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Edit } from '@element-plus/icons-vue'
import { listSku, adjustStock } from '@/api/sku'
import { listProduct } from '@/api/product'
import { formatMoney } from '@/utils/format'

defineOptions({ name: 'Stock' })

const loading = ref(false)
const submitting = ref(false)
const showSearch = ref(true)
const open = ref(false)
const skuList = ref([])
const products = ref([])
const currentRow = ref(null)
const formRef = ref(null)
const queryFormRef = ref(null)

const onlyLowStock = ref(false)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  productId: '',
  keyword: ''
})

const form = ref({
  delta: null,
  reason: '管理端人工调整'
})

const rules = {
  delta: [
    { required: true, message: '请输入变动数量', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('调整数量不能为 0'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

const productOptions = computed(() => products.value)

const filteredList = computed(() => {
  const { productId, keyword } = queryParams.value
  return skuList.value.filter((item) => {
    if (productId && item.productId !== productId) return false
    if (keyword) {
      const hitSpec = (item.spec || '').includes(keyword)
      const hitCode = (item.skuCode || '').includes(keyword)
      if (!hitSpec && !hitCode) return false
    }
    if (onlyLowStock.value && available(item) >= 10) return false
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
 * 可售库存 = 总库存 - 锁定库存
 * 后端 availableStock() 为非 getter 方法，未参与 JSON 序列化，故前端自行计算
 * @param {object} row
 * @returns {number}
 */
function available(row) {
  return Number(row.totalStock || 0) - Number(row.lockedStock || 0)
}

/**
 * 商品 ID 转名称（后端 SKU 只返回 productId）
 * @param {number} productId
 * @returns {string}
 */
function productName(productId) {
  const hit = products.value.find((p) => p.id === productId)
  return hit ? hit.name : '商品 #' + productId
}

/**
 * 查询 SKU 列表
 */
function getList() {
  loading.value = true
  listSku()
    .then((res) => {
      skuList.value = res.rows
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * 加载商品（用于展示商品名与下拉）
 */
function getProducts() {
  listProduct({ pageNum: 1, pageSize: 500 }).then((res) => {
    products.value = res.rows
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
  onlyLowStock.value = false
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, productId: '', keyword: '' }
  getList()
}

/**
 * 打开调整弹窗
 * @param {object} row
 */
function handleAdjust(row) {
  currentRow.value = row
  form.value = { delta: null, reason: '管理端人工调整' }
  open.value = true
}

/**
 * 提交调整
 */
function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) {
      return
    }
    submitting.value = true
    adjustStock(currentRow.value.id, { delta: form.value.delta, reason: form.value.reason })
      .then(() => {
        ElMessage.success('库存调整成功')
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
  getProducts()
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
