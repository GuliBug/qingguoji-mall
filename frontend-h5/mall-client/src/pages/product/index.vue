<template>
  <view class="page">
    <view class="hero">
      <image class="hero-img" :src="coverFor(product.name)" mode="aspectFill" />
      <view class="hero-text">
        <text class="name">{{ product.name }}</text>
        <text class="sub">{{ product.subtitle }}</text>
      </view>
    </view>
    <view class="card">
      <view class="price">¥ {{ selectedSku?.price || '--' }}</view>
      <view class="sec-title">选择规格</view>
      <view class="chips">
        <view v-for="s in skus" :key="s.id" class="chip" :class="{ on: selectedSku?.id === s.id }" @tap="selectedSku = s">
          {{ s.spec }} · ¥{{ s.price }} · {{ s.totalStock - s.lockedStock > 0 ? '有货' : '缺货' }}
        </view>
      </view>
      <view class="sec-title">数量</view>
      <view class="stepper">
        <view class="step-btn" @tap="qty = Math.max(1, qty - 1)">−</view>
        <text>{{ qty }}</text>
        <view class="step-btn" @tap="qty++">＋</view>
      </view>
    </view>
    <view class="bottom-bar">
      <button class="btn-cart" @tap="addCart">加入购物车</button>
      <button class="btn-buy" @tap="buyNow">立即购买</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Taro, { useRouter } from '@tarojs/taro'
import { api, isLoggedIn } from '../../api'
import { coverFor } from '../../coverMap'

const router = useRouter()
const product = ref<any>({})
const skus = ref<any[]>([])
const selectedSku = ref<any>(null)
const qty = ref(1)

async function load() {
  const id = router.params.id
  const res: any = await api.productDetail(Number(id))
  if (res.code === 200) {
    product.value = res.data.product
    skus.value = res.data.skus
    selectedSku.value = res.data.skus[0]
  } else {
    Taro.showToast({ title: res.msg, icon: 'none' })
  }
}

async function addCart() {
  if (!isLoggedIn()) return Taro.showToast({ title: '请先到首页登录', icon: 'none' })
  const res: any = await api.addToCart(selectedSku.value.id, qty.value)
  Taro.showToast({ title: res.code === 200 ? '已加入购物车' : res.msg, icon: res.code === 200 ? 'success' : 'none' })
}

async function buyNow() {
  if (!isLoggedIn()) return Taro.showToast({ title: '请先到首页登录', icon: 'none' })
  await api.addToCart(selectedSku.value.id, qty.value)
  Taro.switchTab({ url: '/pages/cart/index' })
}

load()
</script>

<style lang="scss">
.hero { position: relative;
  .hero-img { width: 100%; height: 480px; display: block; }
  .hero-text {
    position: absolute; left: 0; right: 0; bottom: 0; padding: 60px 40px 30px;
    background: linear-gradient(180deg, rgba(0,0,0,0), rgba(0,0,0,.55)); color: #fff;
    .name { font-size: 40px; font-weight: 700; display: block; letter-spacing: 2px; text-shadow: 0 2px 8px rgba(0,0,0,.4); }
    .sub { font-size: 22px; opacity: .85; display: block; margin-top: 12px; }
  }
}
.price { font-size: 44px; color: #B0231F; font-weight: 700; }
.sec-title { font-size: 24px; color: #9A8F86; margin: 30px 0 16px; }
.chips { display: flex; flex-wrap: wrap; gap: 16px; }
.chip {
  border: 1px solid #E5DACB; background: #FAF6EF; padding: 12px 28px; border-radius: 8px; font-size: 24px;
  &.on { border-color: #B0231F; color: #B0231F; background: #FBEAE9; }
}
.stepper { display: flex; align-items: center; gap: 30px; font-size: 30px;
  .step-btn { width: 56px; height: 56px; border: 1px solid #E5DACB; border-radius: 8px; text-align: center; line-height: 56px; }
}
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; display: flex; padding: 20px; background: #fff;
  .btn-cart, .btn-buy { flex: 1; border-radius: 0; font-size: 28px; }
  .btn-cart { background: #C9912F; color: #fff; }
  .btn-buy { background: linear-gradient(90deg, #C93A32, #8E1714); color: #fff; }
}
</style>
