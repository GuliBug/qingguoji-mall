<template>
  <view class="page">
    <view class="hero">
      <view class="avatar">{{ (nickname || '客')[0] }}</view>
      <view class="info">
        <text class="n">{{ nickname || '未登录' }}</text>
        <text class="lv" v-if="me">LV{{ me.level }} {{ me.levelName }}</text>
      </view>
    </view>
    <view class="pts-row" v-if="me">
      <view class="pt-card"><view class="v">{{ me.member.points }}</view><view class="l">可用积分</view></view>
      <view class="pt-card"><view class="v">{{ me.member.growth }}</view><view class="l">成长值</view></view>
      <view class="pt-card"><view class="v">x{{ me.pointRate }}</view><view class="l">积分倍率</view></view>
    </view>
    <view class="card">
      <view class="cell" @tap="checkin">📅 每日签到（+5 积分）</view>
      <view class="cell" @tap="scan">📷 扫码积分（+10 积分/包）</view>
      <view class="cell" @tap="showLevel">🏅 会员等级：注册→铜→银→金→黑金</view>
      <view class="cell" @tap="showLogs">📄 积分明细</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Taro, { useDidShow } from '@tarojs/taro'
import { api, isLoggedIn } from '../../api'

const me = ref<any>(null)
const nickname = ref('')

async function load() {
  if (!isLoggedIn()) return
  const res: any = await api.me()
  if (res.code === 200) {
    me.value = res.data
    nickname.value = res.data.member.nickname
  }
}

async function checkin() {
  const res: any = await api.checkin()
  Taro.showToast({ title: res.code === 200 ? '签到成功 +5 积分' : res.msg, icon: 'none' })
  if (res.code === 200) load()
}

async function scan() {
  const { result } = await Taro.showModal({
    title: '扫码积分',
    content: '演示：输入包装上的防伪码（种子码形如 QGJ+12位，可到后端日志查看）',
    editable: true,
    placeholderText: 'QGJXXXXXXXXXXXX'
  })
  if (!result?.confirm || !result?.content) return
  const res: any = await api.scan(result.content.trim())
  Taro.showToast({ title: res.code === 200 ? '扫码成功 +10 积分' : res.msg, icon: 'none' })
  if (res.code === 200) load()
}

function showLevel() {
  Taro.showModal({
    title: '会员等级（消费1元=1成长值）',
    content: '注册会员 0 起\n铜卡 1000 · 积分1.2倍\n银卡 5000 · 积分1.5倍 · 95折\n金卡 20000 · 积分2倍 · 9折 · 生日礼\n黑金卡 50000 · 积分2.5倍 · 85折 · 专属客服',
    showCancel: false
  })
}

async function showLogs() {
  const res: any = await api.pointsLogs()
  if (res.code === 200) {
    const lines = res.data.content.slice(0, 5).map((l: any) => `${l.type} ${l.amount > 0 ? '+' : ''}${l.amount}`).join('\n')
    Taro.showModal({ title: '最近积分流水', content: lines || '暂无', showCancel: false })
  }
}

useDidShow(load)
</script>

<style lang="scss">
.hero { background: linear-gradient(160deg, #C93A32, #8E1714); padding: 70px 40px; display: flex; align-items: center; gap: 24px;
  .avatar { width: 110px; height: 110px; border-radius: 50%; background: linear-gradient(135deg, #E8B34B, #C9912F);
    color: #5C3A00; font-size: 48px; font-weight: 700; text-align: center; line-height: 110px; }
  .n { color: #fff; font-size: 34px; font-weight: 600; display: block; }
  .lv { color: #E8B34B; font-size: 22px; display: block; margin-top: 8px; }
}
.pts-row { display: flex; gap: 20px; margin: -40px 24px 0; position: relative;
  .pt-card { flex: 1; background: #fff; border-radius: 20px; padding: 26px 0; text-align: center;
    .v { font-size: 36px; font-weight: 700; color: #B0231F; }
    .l { font-size: 20px; color: #9A8F86; margin-top: 6px; } }
}
.cell { padding: 30px 0; font-size: 28px; border-bottom: 1px solid #F0EAE0; }
.cell:last-child { border: 0; }
</style>
