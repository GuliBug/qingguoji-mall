<template>
  <view class="page">
    <view class="home-head">
      <view class="hh-top">
        <view class="brand">
          <view class="logo">纪</view>
          <view>
            <text class="b-name">青果纪</text>
            <text class="b-sub">QING GUO JI · 匠心青果</text>
          </view>
        </view>
        <view class="hh-right">
          <text class="loc">长沙 · 旗舰店</text>
          <view v-if="!logged" class="login-pill" @tap="doLogin">一键登录</view>
        </view>
      </view>
      <view class="search-input" @tap="toast('搜索功能开发中')">搜索：枸杞青果 / 冰榔 / 礼盒</view>
    </view>

    <scroll-view class="scroll" scroll-y>
      <view class="banner">
        <text class="b-title">秋装新品 · 青果上新</text>
        <text class="b-sub2">枸杞青果系列全新升级 · 会员下单双倍成长值</text>
      </view>

      <view class="member-card" @tap="goMember">
        <view class="mc-top">
          <text class="mc-lv">银卡会员<text class="tag">LV2</text></text>
          <text class="mc-r">会员权益 ›</text>
        </view>
        <view class="mc-pts">{{ points || '8,620' }}<text class="mc-pts-sm"> 积分</text></view>
        <view class="mc-bar"><view class="mc-bar-i"></view></view>
        <view class="mc-next">消费 1 元 = 1 成长值，升级金卡享更多权益</view>
      </view>

      <view class="quick">
        <view class="q" @tap="goMember"><view class="q-ic q-sign">签</view><text>每日签到</text></view>
        <view class="q" @tap="goScan"><view class="q-ic q-scan">扫</view><text>扫码积分</text></view>
        <view class="q" @tap="goPoints"><view class="q-ic q-pts">兑</view><text>积分兑换</text></view>
        <view class="q" @tap="toast('会员礼开发中')"><view class="q-ic q-gift">礼</view><text>会员礼</text></view>
      </view>

      <view class="sec-head">
        <text class="sec-title">爆品热卖</text>
        <text class="sec-more" @tap="toast('进入全部商品')">全部 ›</text>
      </view>
      <view class="grid">
        <view v-for="p in products" :key="p.id" class="gcard" @tap="goDetail(p.id)">
          <image class="gimg" :src="coverFor(p.name)" mode="aspectFill" />
          <view class="gbody">
            <view class="gname">{{ p.name }}</view>
            <view class="grow2">
              <text class="gprice">¥{{ p.price }}</text>
              <text class="gsales">已售 {{ p.sales }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="sec-head">
        <text class="sec-title">礼盒专区</text>
        <text class="sec-more">送礼首选 ›</text>
      </view>
      <view class="grid">
        <view v-for="p in giftProducts" :key="p.id" class="gcard" @tap="goDetail(p.id)">
          <image class="gimg" :src="coverFor(p.name)" mode="aspectFill" />
          <view class="gbody">
            <view class="gname">{{ p.name }}</view>
            <view class="grow2">
              <text class="gprice">¥{{ p.price }}</text>
              <text class="gsales">已售 {{ p.sales }}</text>
            </view>
          </view>
        </view>
        <view v-if="giftProducts.length === 0" class="sec-empty">暂无礼盒商品</view>
      </view>

      <view class="sec-head">
        <text class="sec-title">口味全系列</text>
        <text class="sec-more">更多 ›</text>
      </view>
      <view class="grid" style="margin-bottom: 24px">
        <view v-for="p in otherProducts" :key="p.id" class="gcard" @tap="goDetail(p.id)">
          <image class="gimg" :src="coverFor(p.name)" mode="aspectFill" />
          <view class="gbody">
            <view class="gname">{{ p.name }}</view>
            <view class="grow2">
              <text class="gprice">¥{{ p.price }}</text>
              <text class="gsales">已售 {{ p.sales }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import Taro, { useDidShow } from '@tarojs/taro'
import { api, setToken, isLoggedIn } from '../../api'
import { coverFor, banner } from '../../coverMap'

const products = ref<any[]>([])
const logged = ref(isLoggedIn())
const nickname = ref('')
const points = ref('')

const giftProducts = computed(() => products.value.filter((p) => p.name.includes('礼盒')))
const otherProducts = computed(() => products.value.filter((p) => !p.name.includes('礼盒')))

const goDetail = (id: number) => Taro.navigateTo({ url: `/pages/product/index?id=${id}` })
const goMember = () => Taro.switchTab({ url: '/pages/member/index' })
const goScan = () => Taro.navigateTo({ url: '/pages/scan/index' })
const goPoints = () => Taro.navigateTo({ url: '/pages/points/index' })
function toast(m: string) {
  Taro.showToast({ title: m, icon: 'none' })
}

async function load() {
  const res: any = await api.products()
  if (res.code === 200) products.value = res.data.content
}

async function doLogin() {
  const { confirm } = await Taro.showModal({
    title: '登录',
    content: '使用演示手机号 13800001111 / 验证码 123456 登录？'
  })
  if (!confirm) return
  const res: any = await api.login('13800001111', '123456')
  if (res.code === 200) {
    setToken(res.data.token)
    logged.value = true
    nickname.value = res.data.member.nickname
    Taro.showToast({ title: '登录成功', icon: 'success' })
  } else {
    Taro.showToast({ title: res.msg, icon: 'none' })
  }
}

useDidShow(async () => {
  logged.value = isLoggedIn()
  await load()
  if (logged.value) {
    const me: any = await api.me()
    if (me.code === 200) {
      nickname.value = me.data.member.nickname
      points.value = String(me.data.member.points)
    }
  }
})
</script>

<style lang="scss">
$red: #b0231f;
$red-deep: #8e1714;
$gold: #e8b34b;
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
.home-head {
  background: linear-gradient(160deg, #c93a32, #8e1714 70%);
  padding: 28px 32px 120px;
  color: #fff;
  .hh-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
  }
  .brand {
    display: flex;
    align-items: center;
    gap: 16px;
    .logo {
      width: 64px;
      height: 64px;
      border-radius: 16px;
      background: linear-gradient(135deg, $gold, $gold-deep);
      color: #5c3a00;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 30px;
    }
    .b-name {
      font-size: 34px;
      font-weight: 700;
      letter-spacing: 2px;
      display: block;
    }
    .b-sub {
      font-size: 18px;
      opacity: 0.65;
      display: block;
      letter-spacing: 1px;
    }
  }
  .loc {
    font-size: 22px;
    opacity: 0.8;
  }
  .login-pill {
    background: linear-gradient(135deg, $gold, $gold-deep);
    color: #5c3a00;
    font-size: 22px;
    padding: 8px 24px;
    border-radius: 30px;
    font-weight: 600;
  }
  .search-input {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 32px;
    height: 64px;
    display: flex;
    align-items: center;
    padding: 0 24px;
    color: #b8a99c;
    font-size: 24px;
  }
}
.scroll {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 120px;
}
.banner {
  margin: -90px 28px 0;
  background: linear-gradient(120deg, #2b2320, #4a3226);
  border-radius: 24px;
  height: 200px;
  color: $gold;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 40px;
  position: relative;
  overflow: hidden;
  .b-title {
    font-size: 34px;
    letter-spacing: 1px;
    font-weight: 700;
  }
  .b-sub2 {
    font-size: 22px;
    opacity: 0.75;
    margin-top: 12px;
    color: #d8c9a8;
  }
}
.member-card {
  margin: 24px 28px;
  border-radius: 24px;
  padding: 28px;
  color: #4a2e00;
  background: linear-gradient(120deg, #f3d288, #e8b34b 55%, #d69e35);
  position: relative;
  overflow: hidden;
  .mc-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .mc-lv {
    font-size: 30px;
    font-weight: 700;
    letter-spacing: 1px;
    .tag {
      background: rgba(92, 58, 0, 0.18);
      color: #4a2e00;
      margin-left: 12px;
      font-size: 20px;
      padding: 2px 12px;
      border-radius: 8px;
    }
  }
  .mc-r {
    font-size: 22px;
    background: rgba(92, 58, 0, 0.14);
    padding: 8px 20px;
    border-radius: 24px;
  }
  .mc-pts {
    font-size: 44px;
    font-weight: 700;
    margin-top: 20px;
    .mc-pts-sm {
      font-size: 22px;
      font-weight: 400;
      opacity: 0.7;
    }
  }
  .mc-bar {
    height: 10px;
    border-radius: 6px;
    background: rgba(92, 58, 0, 0.18);
    margin-top: 16px;
    overflow: hidden;
    .mc-bar-i {
      display: block;
      height: 100%;
      width: 38%;
      background: linear-gradient(90deg, #8e1714, #c93a32);
      border-radius: 6px;
    }
  }
  .mc-next {
    font-size: 20px;
    opacity: 0.75;
    margin-top: 10px;
  }
}
.quick {
  display: flex;
  margin: 0 28px;
  background: #fff;
  border-radius: 24px;
  padding: 28px 0;
  .q {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    font-size: 22px;
    color: #6b5d52;
    .q-ic {
      width: 72px;
      height: 72px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-style: normal;
      font-size: 32px;
      font-weight: 700;
      color: #fff;
      &.q-sign {
        background: linear-gradient(135deg, $gold, $gold-deep);
      }
      &.q-scan {
        background: linear-gradient(135deg, #c93a32, #8e1714);
      }
      &.q-pts {
        background: linear-gradient(135deg, #2b2320, #4a3226);
      }
      &.q-gift {
        background: linear-gradient(135deg, #1989fa, #0e6fd8);
      }
    }
  }
}
.sec-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 32px 32px 20px;
  .sec-title {
    font-size: 30px;
    font-weight: 600;
    display: flex;
    align-items: center;
    &::before {
      content: '';
      width: 8px;
      height: 28px;
      border-radius: 4px;
      background: linear-gradient($red, $gold);
      margin-right: 12px;
    }
  }
  .sec-more {
    font-size: 22px;
    color: $sub;
  }
}
.sec-empty {
  width: 100%;
  text-align: center;
  color: $sub;
  font-size: 24px;
  padding: 40px 0;
}
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin: 0 28px;
}
.gcard {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  .gimg {
    width: 100%;
    height: 240px;
    display: block;
  }
  .gbody {
    padding: 18px 20px 22px;
    .gname {
      font-size: 26px;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .grow2 {
      display: flex;
      align-items: baseline;
      margin-top: 10px;
    }
    .gprice {
      font-size: 32px;
      color: $red;
      font-weight: 600;
    }
    .gsales {
      font-size: 20px;
      color: $sub;
      margin-left: 12px;
    }
  }
}
</style>
