<template>
  <view class="page">
    <view v-if="items.length === 0" class="empty">购物车是空的，去首页逛逛吧</view>
    <view v-for="it in items" :key="it.cartId" class="cart-item" :class="{ invalid: !it.onSale }">
      <image class="ci-img" :src="coverFor(it.productName)" mode="aspectFill" />
      <view class="ci-body">
        <view class="ci-n">{{ it.productName }}</view>
        <view class="ci-sku">{{ it.spec }}</view>
        <view class="ci-row">
          <text class="price">¥{{ it.price }}</text>
          <view class="stepper">
            <view class="step-btn" @tap="changeQty(it, it.qty - 1)">−</view>
            <text>{{ it.qty }}</text>
            <view class="step-btn" @tap="changeQty(it, it.qty + 1)">＋</view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="items.length" class="settle">
      <text>合计：<text class="price">¥{{ total.toFixed(2) }}</text></text>
      <button class="btn-red" @tap="submitOrder">提交订单</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import Taro, { useDidShow } from '@tarojs/taro'
import { api, isLoggedIn } from '../../api'
import { coverFor } from '../../coverMap'

const items = ref<any[]>([])
const shortName = (n: string) => n.replace(/槟榔|青果/g, '').slice(0, 4)
const total = computed(() =>
  items.value.filter(i => i.onSale).reduce((s, i) => s + i.price * i.qty, 0))

async function load() {
  if (!isLoggedIn()) { Taro.showToast({ title: '请先到首页登录', icon: 'none' }); return }
  const res: any = await api.cart()
  if (res.code === 200) items.value = res.data
}

async function changeQty(it: any, qty: number) {
  const res: any = await api.updateCart(it.cartId, qty)
  if (res.code === 200) load()
}

async function submitOrder() {
  const lines = items.value.filter(i => i.onSale).map(i => ({ skuId: i.skuId, qty: i.qty }))
  if (!lines.length) return Taro.showToast({ title: '没有可结算商品', icon: 'none' })
  const order: any = await api.createOrder(lines, '张三', '13800001111', '湖南省长沙市岳麓区青果大厦8F')
  if (order.code !== 200) return Taro.showToast({ title: order.msg, icon: 'none' })
  const { result } = await Taro.showModal({
    title: '下单成功',
    content: `订单号 ${order.data.orderNo}\n实付 ¥${order.data.payAmount}（含运费 ¥${order.data.freight}）\n立即模拟支付？`
  })
  if (!result?.confirm) return
  const pay: any = await api.pay(order.data.orderNo)
  if (pay.code === 200) {
    Taro.showModal({ title: '支付成功', content: `状态：${pay.data.status}\n获得积分 +${pay.data.pointsEarned}`, showCancel: false })
    load()
  } else {
    Taro.showToast({ title: pay.msg, icon: 'none' })
  }
}

useDidShow(load)
</script>

<style lang="scss">
.cart-item { display: flex; gap: 20px; background: #fff; padding: 24px; margin: 0 24px 4px; align-items: center;
  &.invalid { opacity: .5; }
  .ci-img { width: 140px; height: 140px; border-radius: 16px; display: block; flex-shrink: 0; }
  .ci-body { flex: 1; }
  .ci-n { font-size: 26px; }
  .ci-sku { font-size: 20px; color: #9A8F86; background: #F5F1E8; display: inline-block; padding: 4px 12px; border-radius: 6px; margin: 10px 0; }
  .ci-row { display: flex; justify-content: space-between; align-items: center; }
  .price { color: #B0231F; font-weight: 600; font-size: 28px; }
  .stepper { display: flex; align-items: center; gap: 20px;
    .step-btn { width: 48px; height: 48px; border: 1px solid #E5DACB; border-radius: 8px; text-align: center; line-height: 48px; } }
}
.settle { position: fixed; bottom: 100px; left: 0; right: 0; background: #fff; padding: 20px 30px;
  display: flex; justify-content: space-between; align-items: center; font-size: 28px; }
.empty { text-align: center; color: #9A8F86; padding: 200px 0; }
</style>
