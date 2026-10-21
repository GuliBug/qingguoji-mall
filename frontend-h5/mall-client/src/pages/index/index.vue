<template>
  <view class="page">
    <view class="head">
      <text class="brand">青果纪</text>
      <text class="slogan">QING GUO JI · 匠心青果</text>
      <view v-if="!logged" class="login-btn" @tap="doLogin">一键登录</view>
      <view v-else class="member-info">欢迎，{{ nickname }}</view>
    </view>

    <image v-if="banner" class="banner" :src="banner" mode="aspectFill" />

    <view class="goods-list">
      <view v-for="p in products" :key="p.id" class="goods-card" @tap="goDetail(p.id)">
        <image class="goods-img" :src="coverFor(p.name)" mode="aspectFill" />
        <view class="goods-name">{{ p.name }}</view>
        <view class="goods-sub">{{ p.subtitle }}</view>
        <view class="goods-sales">已售 {{ p.sales }}</view>
      </view>
    </view>
    <view v-if="products.length === 0" class="empty">加载中…</view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Taro, { useDidShow } from '@tarojs/taro'
import { api, getToken, setToken, isLoggedIn } from '../../api'
import { coverFor, banner } from '../../coverMap'

const products = ref<any[]>([])
const logged = ref(isLoggedIn())
const nickname = ref('')

const shortName = (n: string) => n.replace(/槟榔|青果/g, '').slice(0, 4)
const goDetail = (id: number) => Taro.navigateTo({ url: `/pages/product/index?id=${id}` })

async function load() {
  const res: any = await api.products()
  if (res.code === 200) products.value = res.data.content
}

async function doLogin() {
  const { result } = await Taro.showModal({ title: '登录', content: '使用演示手机号 13800001111 / 验证码 123456 登录？' })
  if (!result?.confirm) return
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
    if (me.code === 200) nickname.value = me.data.member.nickname
  }
})
</script>

<style lang="scss">
.page { padding-bottom: 30px; }
.head {
  background: linear-gradient(160deg, #C93A32, #8E1714);
  padding: 60px 40px 50px;
  color: #fff;
  .brand { font-size: 44px; font-weight: 700; letter-spacing: 4px; display: block; }
  .slogan { font-size: 20px; opacity: .7; display: block; margin-top: 8px; }
  .login-btn {
    display: inline-block; margin-top: 24px; background: linear-gradient(135deg, #E8B34B, #C9912F);
    color: #5C3A00; padding: 12px 40px; border-radius: 40px; font-weight: 600;
  }
  .member-info { display: inline-block; margin-top: 24px; font-size: 26px; opacity: .9; }
}
.banner { width: 100%; height: 300px; display: block; }
.goods-list { display: flex; flex-wrap: wrap; padding: 24px; gap: 20px; }
.goods-card {
  width: calc(50% - 10px); background: #fff; border-radius: 20px; overflow: hidden;
  .goods-img {
    width: 100%; height: 260px; display: block;
  }
  .goods-name { font-size: 26px; padding: 16px 20px 4px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
  .goods-sub { font-size: 20px; color: #9A8F86; padding: 0 20px; }
  .goods-sales { font-size: 20px; color: #9A8F86; padding: 8px 20px 20px; }
}
.empty { text-align: center; color: #9A8F86; padding: 100px 0; }
</style>
