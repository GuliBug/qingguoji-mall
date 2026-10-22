<template>
  <view class="page">
    <view class="nav">购物车（{{ items.length }}）</view>

    <scroll-view v-if="items.length" class="scroll" scroll-y>
      <view
        v-for="it in items"
        :key="it.cartId"
        class="cart-item"
        :class="{ invalid: !it.onSale }"
        @tap="toggle(it)"
      >
        <view class="ck" :class="{ on: it.onSale && selected.has(it.cartId) }"></view>
        <image class="ci-img" :src="coverFor(it.productName)" mode="aspectFill" />
        <view class="ci-body">
          <view class="ci-n">{{ it.productName }}</view>
          <view class="ci-sku">{{ it.spec }}</view>
          <view class="ci-row">
            <text class="price">¥{{ it.price }}</text>
            <view class="stepper" @tap.stop>
              <view class="step-btn" @tap="changeQty(it, it.qty - 1)">−</view>
              <text>{{ it.qty }}</text>
              <view class="step-btn" @tap="changeQty(it, it.qty + 1)">＋</view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view v-else class="empty">购物车是空的，去首页逛逛吧</view>

    <view v-if="items.length" class="settle">
      <view class="all" @tap="toggleAll">
        <view class="ck" :class="{ on: allSelected }"></view>
        <text>全选</text>
      </view>
      <view class="sum">
        合计：<text class="sum-b">¥{{ total.toFixed(2) }}</text>
      </view>
      <button class="btn-red" @tap="checkout">结算({{ selectedCount }})</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import Taro, { useDidShow } from '@tarojs/taro'
import { api, isLoggedIn } from '../../api'
import { coverFor } from '../../coverMap'

const items = ref<any[]>([])
const selected = ref<Set<number>>(new Set())

const selectedCount = computed(() => items.value.filter((i) => i.onSale && selected.value.has(i.cartId)).length)
const total = computed(() =>
  items.value.filter((i) => i.onSale && selected.value.has(i.cartId)).reduce((s, i) => s + i.price * i.qty, 0)
)
const allSelected = computed(
  () => items.value.filter((i) => i.onSale).length > 0 && items.value.filter((i) => i.onSale).every((i) => selected.value.has(i.cartId))
)

function syncSelection() {
  const next = new Set<number>()
  items.value.filter((i) => i.onSale).forEach((i) => next.add(i.cartId))
  selected.value = next
}

async function load() {
  if (!isLoggedIn()) {
    Taro.showToast({ title: '请先到首页登录', icon: 'none' })
    return
  }
  const res: any = await api.cart()
  if (res.code === 200) {
    items.value = res.data
    syncSelection()
  }
}

function toggle(it: any) {
  if (!it.onSale) return
  const next = new Set(selected.value)
  next.has(it.cartId) ? next.delete(it.cartId) : next.add(it.cartId)
  selected.value = next
}
function toggleAll() {
  selected.value = allSelected.value ? new Set() : new Set(items.value.filter((i) => i.onSale).map((i) => i.cartId))
}

async function changeQty(it: any, qty: number) {
  const res: any = await api.updateCart(it.cartId, qty)
  if (res.code === 200) load()
}

function checkout() {
  const lines = items.value
    .filter((i) => i.onSale && selected.value.has(i.cartId))
    .map((i) => ({ skuId: i.skuId, qty: i.qty, cartId: i.cartId }))
  if (!lines.length) return Taro.showToast({ title: '请选择要结算的商品', icon: 'none' })
  Taro.setStorageSync('checkout_lines', lines)
  Taro.navigateTo({ url: '/pages/confirm/index' })
}

useDidShow(load)
</script>

<style lang="scss">
$red: #b0231f;
$red-deep: #8e1714;
$gold-deep: #c9912f;
$bg: #f7f3ec;
$text: #2b2320;
$sub: #9a8f86;
$line: #eee7dd;

.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: $bg;
}
.nav {
  height: 88px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: 600;
  flex-shrink: 0;
  border-bottom: 1px solid $line;
}
.scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0 140px;
}
.cart-item {
  display: flex;
  gap: 20px;
  background: #fff;
  padding: 24px;
  margin: 0 24px 8px;
  align-items: center;
  border-radius: 16px;
  &.invalid {
    opacity: 0.5;
  }
  .ck {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    border: 1px solid #d8cbbb;
    flex-shrink: 0;
    &.on {
      background: $red;
      border-color: $red;
      position: relative;
      &::after {
        content: '';
        position: absolute;
        left: 11px;
        top: 6px;
        width: 10px;
        height: 18px;
        border: solid #fff;
        border-width: 0 4px 4px 0;
        transform: rotate(45deg);
      }
    }
  }
  .ci-img {
    width: 140px;
    height: 140px;
    border-radius: 16px;
    display: block;
    flex-shrink: 0;
  }
  .ci-body {
    flex: 1;
  }
  .ci-n {
    font-size: 28px;
  }
  .ci-sku {
    font-size: 20px;
    color: $sub;
    background: #f5f1e8;
    display: inline-block;
    padding: 4px 12px;
    border-radius: 6px;
    margin: 10px 0;
  }
  .ci-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .price {
    color: $red;
    font-weight: 600;
    font-size: 30px;
  }
  .stepper {
    display: flex;
    align-items: center;
    gap: 20px;
    .step-btn {
      width: 48px;
      height: 48px;
      border: 1px solid #e5dacb;
      border-radius: 8px;
      text-align: center;
      line-height: 48px;
      font-size: 30px;
    }
  }
}
.settle {
  position: fixed;
  bottom: 100px;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20px 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 28px;
  border-top: 1px solid $line;
  .all {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 26px;
    color: #666;
    .ck {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      border: 1px solid #d8cbbb;
      &.on {
        background: $red;
        border-color: $red;
        position: relative;
        &::after {
          content: '';
          position: absolute;
          left: 11px;
          top: 6px;
          width: 10px;
          height: 18px;
          border: solid #fff;
          border-width: 0 4px 4px 0;
          transform: rotate(45deg);
        }
      }
    }
  }
  .sum {
    flex: 1;
    text-align: right;
    font-size: 24px;
    .sum-b {
      color: $red;
      font-size: 34px;
      font-weight: 700;
    }
  }
  .btn-red {
    background: linear-gradient(90deg, #c93a32, $red-deep);
    color: #fff;
    border: 0;
    padding: 20px 40px;
    border-radius: 40px;
    font-size: 28px;
  }
}
.empty {
  text-align: center;
  color: $sub;
  padding: 200px 0;
}
</style>
