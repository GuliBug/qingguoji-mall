<template>
  <view class="page">
    <view class="nav">
      <text class="back" @tap="goBack">‹</text>
      <text class="nav-title">商品详情</text>
    </view>

    <scroll-view class="scroll" scroll-y>
      <view class="d-img">
        <text class="d-name-lg">{{ product.name }}</text>
        <text class="d-sub-lg">QINGGUOJI · 匠心卤制</text>
      </view>

      <view class="d-body">
        <view class="d-price">
          <text class="p">¥{{ selectedSku?.price || '--' }}</text>
          <text class="o">¥{{ Math.round((selectedSku?.price || 0) * 1.2) }}</text>
          <text class="g">下单得 {{ Math.round((selectedSku?.price || 0) * 2) }} 积分</text>
        </view>
        <view class="d-name">{{ product.name }}</view>
        <view class="d-sub">会员专享：银卡95折 · 金卡9折 · 黑金85折</view>
        <view class="d-svc">
          <text>正品保证</text>
          <text>扫码积分</text>
          <text>破损包赔</text>
          <text>48h发货</text>
        </view>
      </view>

      <view class="d-sec">
        <view class="sku-title">规格</view>
        <view class="chips">
          <view
            v-for="s in skus"
            :key="s.id"
            class="chip"
            :class="{ on: selectedSku?.id === s.id }"
            @tap="selectedSku = s"
          >
            {{ s.spec }} · ¥{{ s.price }} · {{ s.totalStock - s.lockedStock > 0 ? '有货' : '缺货' }}
          </view>
        </view>

        <view class="sku-title">口味</view>
        <view class="chips">
          <view
            v-for="f in flavors"
            :key="f"
            class="chip"
            :class="{ on: flavor === f }"
            @tap="flavor = f"
          >{{ f }}</view>
        </view>

        <view class="sku-title">购买数量</view>
        <view class="stepper">
          <view class="step-btn" @tap="qty = Math.max(1, qty - 1)">−</view>
          <text class="qty">{{ qty }}</text>
          <view class="step-btn" @tap="qty++">＋</view>
        </view>
      </view>

      <view class="d-sec detail-block">
        <view class="sku-title">商品详情</view>
        <view class="detail-ph">产地 / 卤制工艺 / 检验报告 图文详情</view>
      </view>
    </scroll-view>

    <view class="d-bar">
      <view class="mini" @tap="goMember"><view class="mini-ic">会</view><text>会员</text></view>
      <view class="mini" @tap="goCart"><view class="mini-ic">车</view><text>购物车</text></view>
      <view class="grow"></view>
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
const flavor = ref('枸杞青果')
const flavors = ['枸杞青果', '咖啡金花', '冰榔薄荷']

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

function goBack() {
  Taro.navigateBack()
}
function goMember() {
  Taro.switchTab({ url: '/pages/member/index' })
}
function goCart() {
  Taro.switchTab({ url: '/pages/cart/index' })
}

async function addCart() {
  if (!isLoggedIn()) return Taro.showToast({ title: '请先到首页登录', icon: 'none' })
  const res: any = await api.addToCart(selectedSku.value.id, qty.value)
  if (res.code === 200) {
    Taro.showToast({ title: '已加入购物车', icon: 'success' })
  } else {
    Taro.showToast({ title: res.msg, icon: 'none' })
  }
}

async function buyNow() {
  if (!isLoggedIn()) return Taro.showToast({ title: '请先到首页登录', icon: 'none' })
  const res: any = await api.addToCart(selectedSku.value.id, qty.value)
  if (res.code === 200) Taro.switchTab({ url: '/pages/cart/index' })
  else Taro.showToast({ title: res.msg, icon: 'none' })
}

load()
</script>

<style lang="scss">
$red: #b0231f;
$red-deep: #8e1714;
$gold: #e8b34b;
$gold-deep: #c9912f;
$bg: #f7f3ec;
$card: #fff;
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
  position: relative;
  flex-shrink: 0;
  border-bottom: 1px solid $line;
  .back {
    position: absolute;
    left: 20px;
    font-size: 48px;
    color: #333;
    line-height: 1;
  }
  .nav-title {
    font-size: 32px;
    font-weight: 600;
  }
}
.scroll {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 120px;
}
.d-img {
  height: 520px;
  background: linear-gradient(135deg, #8e1714, #c93a32);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .d-name-lg {
    font-size: 48px;
    font-weight: 700;
    letter-spacing: 3px;
  }
  .d-sub-lg {
    font-size: 22px;
    opacity: 0.7;
    margin-top: 14px;
    letter-spacing: 1px;
  }
}
.d-body {
  background: #fff;
  padding: 28px;
  border-radius: 24px 24px 0 0;
  margin-top: -24px;
  position: relative;
  .d-price {
    display: flex;
    align-items: baseline;
    flex-wrap: wrap;
    .p {
      font-size: 48px;
      color: $red;
      font-weight: 700;
    }
    .o {
      font-size: 24px;
      color: #bbb;
      text-decoration: line-through;
      margin-left: 16px;
    }
    .g {
      font-size: 22px;
      color: $gold-deep;
      background: #fbf3e1;
      padding: 4px 16px;
      border-radius: 20px;
      margin-left: 16px;
    }
  }
  .d-name {
    font-size: 32px;
    font-weight: 600;
    margin: 16px 0 12px;
  }
  .d-sub {
    font-size: 22px;
    color: $gold-deep;
    background: #fbf3e1;
    padding: 8px 16px;
    border-radius: 8px;
    display: inline-block;
  }
  .d-svc {
    display: flex;
    gap: 28px;
    font-size: 22px;
    color: #8a7d72;
    padding: 20px 0;
    border-bottom: 1px solid $line;
  }
}
.d-sec {
  background: #fff;
  margin-top: 20px;
  padding: 28px;
  .sku-title {
    font-size: 26px;
    color: $sub;
    margin-bottom: 20px;
  }
  .chips {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 28px;
  }
  .chip {
    border: 1px solid #e5dacb;
    border-radius: 8px;
    padding: 12px 28px;
    font-size: 24px;
    background: #faf6ef;
    &.on {
      border-color: $red;
      color: $red;
      background: #fbeae9;
    }
  }
  .stepper {
    display: flex;
    align-items: center;
    gap: 30px;
    font-size: 32px;
    .step-btn {
      width: 56px;
      height: 56px;
      border: 1px solid #e5dacb;
      border-radius: 8px;
      text-align: center;
      line-height: 56px;
    }
    .qty {
      min-width: 40px;
      text-align: center;
    }
  }
  &.detail-block {
    margin-bottom: 20px;
    border-radius: 0 0 24px 24px;
  }
  .detail-ph {
    height: 200px;
    border-radius: 16px;
    background: #faf6ef;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #c9bcae;
    font-size: 26px;
  }
}
.d-bar {
  position: fixed;
  bottom: 100px;
  left: 0;
  right: 0;
  height: 100px;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 16px;
  z-index: 40;
  border-top: 1px solid $line;
  .mini {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 18px;
    color: #8a7d72;
    width: 60px;
    .mini-ic {
      width: 40px;
      height: 40px;
      background: #c9bcae;
      border-radius: 10px;
      margin-bottom: 4px;
      color: #fff;
      text-align: center;
      line-height: 40px;
      font-size: 22px;
    }
  }
  .grow {
    flex: 1;
  }
  .btn-cart,
  .btn-buy {
    border: 0;
    padding: 22px 36px;
    font-size: 26px;
    color: #fff;
  }
  .btn-cart {
    background: $gold-deep;
    border-radius: 44px 0 0 44px;
  }
  .btn-buy {
    background: linear-gradient(90deg, #c93a32, $red-deep);
    border-radius: 0 44px 44px 0;
  }
}
</style>
