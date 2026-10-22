<template>
  <div class="page-wrapper">
    <el-form v-show="showSearch" ref="queryFormRef" :model="queryParams" :inline="true" class="search-card">
      <el-form-item label="商品名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入商品名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="全部分类" clearable style="width: 160px">
          <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="item in PRODUCT_STATUS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-card">
      <div class="table-toolbar">
        <div class="toolbar-title">商品列表</div>
        <RightToolbar v-model:show-search="showSearch" @query-table="getList" />
      </div>

      <el-table v-loading="loading" :data="pagedList" border>
        <el-table-column label="ID" prop="id" width="70" align="center" />
        <el-table-column label="封面" width="80" align="center">
          <template #default="scope">
            <span class="cover-dot" :style="{ background: scope.row.cover || '#ccc' }" />
          </template>
        </el-table-column>
        <el-table-column label="商品名称" prop="name" min-width="180" show-overflow-tooltip />
        <el-table-column label="分类" prop="category" width="110" align="center" />
        <el-table-column label="价格区间" width="160" align="center">
          <template #default="scope">
            <span v-if="priceRange(scope.row.id)" class="amount">{{ priceRange(scope.row.id) }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="销量" prop="sales" width="90" align="center" />
        <el-table-column label="积分规则" width="110" align="center">
          <template #default="scope">
            <el-tag size="small" type="info">{{ scope.row.pointsFlag || '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <DictTag :options="PRODUCT_STATUS" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center" show-overflow-tooltip>
          <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 1"
              link
              type="danger"
              :icon="Bottom"
              @click="handleChangeStatus(scope.row, 0)"
            >
              下架
            </el-button>
            <el-button v-else link type="primary" :icon="Top" @click="handleChangeStatus(scope.row, 1)">上架</el-button>
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
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Top, Bottom } from '@element-plus/icons-vue'
import { listProduct, changeProductStatus } from '@/api/product'
import { listSku } from '@/api/sku'
import { PRODUCT_STATUS } from '@/utils/dict'
import { formatDateTime } from '@/utils/format'

defineOptions({ name: 'Product' })

const loading = ref(false)
const showSearch = ref(true)
const productList = ref([])
const skus = ref([])
const queryFormRef = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  name: '',
  category: '',
  status: ''
})

// 分类下拉（由商品数据推导，后端无字典接口）
const categories = computed(() => [...new Set(productList.value.map((p) => p.category).filter(Boolean))])

// 前端过滤（后端 /products 不支持按名称/分类/状态查询，故一次拉全量后本地过滤）
const filteredList = computed(() => {
  const { name, category, status } = queryParams.value
  return productList.value.filter((item) => {
    if (name && !(item.name || '').includes(name)) return false
    if (category && item.category !== category) return false
    if (status !== '' && status !== null && status !== undefined && item.status !== status) return false
    return true
  })
})

const total = computed(() => filteredList.value.length)

// 本地分页
const pagedList = computed(() => {
  const { pageNum, pageSize } = queryParams.value
  const start = (pageNum - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
})

/**
 * 价格区间：由 SKU 聚合（商品实体本身没有价格字段）
 * @param {number} productId
 * @returns {string}
 */
function priceRange(productId) {
  const list = skus.value.filter((s) => s.productId === productId)
  if (list.length === 0) {
    return ''
  }
  const prices = list.map((s) => Number(s.price))
  const min = Math.min(...prices)
  const max = Math.max(...prices)
  return min === max ? `¥${min.toFixed(2)}` : `¥${min.toFixed(2)} ~ ¥${max.toFixed(2)}`
}

/**
 * 查询商品列表（后端仅支持分页，一次拉全量后本地过滤分页）
 */
function getList() {
  loading.value = true
  listProduct({ pageNum: 1, pageSize: 500 })
    .then((res) => {
      productList.value = res.rows
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * 查询 SKU（仅用于聚合价格）
 */
function getSkuList() {
  listSku().then((res) => {
    skus.value = res.rows
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
 * 重置搜索
 */
function resetQuery() {
  queryFormRef.value && queryFormRef.value.resetFields()
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, name: '', category: '', status: '' }
  getList()
}

/**
 * 上架 / 下架
 * @param {object} row
 * @param {number} status
 */
function handleChangeStatus(row, status) {
  const action = status === 1 ? '上架' : '下架'
  ElMessageBox.confirm(`确认${action}商品「${row.name}」吗？`, '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => changeProductStatus(row.id, status))
    .then(() => {
      ElMessage.success(`${action}成功`)
      getList()
    })
    .catch(() => {})
}

onMounted(() => {
  getList()
  getSkuList()
})
</script>

<style lang="scss" scoped>
.cover-dot {
  display: inline-block;
  width: 22px;
  height: 22px;
  border-radius: 4px;
  vertical-align: middle;
}

.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
</style>
