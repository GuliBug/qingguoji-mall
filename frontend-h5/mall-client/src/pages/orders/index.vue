<template>
  <view class="page">
    <view class="tabs">
      <view v-for="t in tabs" :key="t.v" class="tab" :class="{ on: cur === t.v }" @tap="switchTab(t.v)">{{ t.n }}</view>
    </view>
    <view v-if="orders.length === 0" class="empty">暂无订单</view>
    <view v-for="o in orders" :key="o.id" class="order-card">
      <view class="oc-head">
        <text class="no">{{ o.orderNo }}</text>
        <text class="status">{{ statusName(o.status) }}</text>
      </view>
      <view v-for="it in o.items" :key="it.id" class="oc-item">
        <text class="n">{{ it.productName }}（{{ it.spec }}）</text>
        <text class="q">x{{ it.qty }}</text>
        <text class="p">¥{{ it.payAmount }}</text>
      </view>
      <view class="oc-foot">
        <text class="t">实付 <text class="price">¥{{ o.payAmount }}</text>（含运费¥{{ o.freight }}，积分+{{ o.pointsEarned || 0 }}）</text>
        <button v-if="o.status === 'WAIT_PAY'" class="mini-btn" @tap="doPay(o)">支付</button>
        <button v-if="o.status === 'WAIT_PAY'" class="mini-btn gray" @tap="doCancel(o)">取消</button>
        <button v-if="o.status === 'SHIPPED'" class="mini-btn" @tap="doConfirm(o)">确认收货</button>
        <button v-if="['PAID','SHIPPED','COMPLETED'].includes(o.status)" class="mini-btn gray" @tap="doAfterSale(o)">售后</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Taro, { useDidShow } from '@tarojs/taro'
import { api } from '../../api'

const tabs = [
  { v: '', n: '全部' }, { v: 'WAIT_PAY', n: '待付款' }, { v: 'PAID', n: '待发货' },
  { v: 'SHIPPED', n: '待收货' }, { v: 'COMPLETED', n: '已完成' }, { v: 'CANCELED', n: '已取消' }
]
const cur = ref('')
const orders = ref<any[]>([])

const statusName = (s: string) => ({
  WAIT_PAY: '待付款', PAID: '待发货', SHIPPED: '待收货',
  COMPLETED: '已完成', CANCELED: '已取消', AFTER_SALE: '售后中'
} as Record<string, string>)[s] || s

async function load() {
  const res: any = await api.orders(cur.value)
  if (res.code === 200) orders.value = res.data.content
}

function switchTab(v: string) { cur.value = v; load() }

async function doPay(o: any) {
  const res: any = await api.pay(o.orderNo)
  if (res.code === 200) { Taro.showToast({ title: `支付成功 积分+${res.data.pointsEarned}`, icon: 'success' }); load() }
  else Taro.showToast({ title: res.msg, icon: 'none' })
}

async function doCancel(o: any) {
  const res: any = await api.cancel(o.orderNo)
  if (res.code === 200) { Taro.showToast({ title: '已取消，库存已释放', icon: 'none' }); load() }
}

async function doConfirm(o: any) {
  const res: any = await api.confirm(o.orderNo)
  if (res.code === 200) { Taro.showToast({ title: '已确认收货', icon: 'success' }); load() }
}

async function doAfterSale(o: any) {
  const it = o.items[0]
  const { result } = await Taro.showModal({
    title: '申请售后',
    content: `${it.productName}\n仅退款=未收到货；退货退款=已收到货\n输入售后数量（最多 ${it.qty - it.afterSaleQty}）`,
    editable: true, placeholderText: '1'
  })
  if (!result?.confirm) return
  const type = o.status === 'PAID' ? 'REFUND_ONLY' : 'RETURN_REFUND'
  const res: any = await api.applyAfterSale(it.id, type, Number(result.content) || 1, '七天无理由')
  Taro.showToast({ title: res.code === 200 ? `售后单 ${res.data.afterNo} 已提交` : res.msg, icon: 'none' })
  if (res.code === 200) load()
}

useDidShow(load)
</script>

<style lang="scss">
.tabs { display: flex; background: #fff; padding: 0 10px;
  .tab { flex: 1; text-align: center; padding: 22px 0; font-size: 24px; color: #666;
    &.on { color: #B0231F; font-weight: 600; } } }
.order-card { background: #fff; border-radius: 20px; margin: 20px 24px; padding: 24px;
  .oc-head { display: flex; justify-content: space-between; font-size: 22px; color: #9A8F86;
    .status { color: #B0231F; } }
  .oc-item { display: flex; justify-content: space-between; font-size: 24px; padding: 12px 0;
    .n { flex: 1; } }
  .oc-foot { display: flex; justify-content: flex-end; gap: 16px; align-items: center; padding-top: 16px; border-top: 1px solid #F0EAE0;
    .t { font-size: 22px; margin-right: auto; }
    .price { color: #B0231F; }
    .mini-btn { font-size: 22px; padding: 6px 24px; border-radius: 30px; background: linear-gradient(90deg, #C93A32, #8E1714); color: #fff;
      &.gray { background: #fff; color: #666; border: 1px solid #ccc; } } }
}
.empty { text-align: center; color: #9A8F86; padding: 200px 0; }
</style>
