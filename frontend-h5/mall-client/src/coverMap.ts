import p1 from './assets/products/p1.png'
import p2 from './assets/products/p2.png'
import p3 from './assets/products/p3.png'
import p4 from './assets/products/p4.png'
import p5 from './assets/products/p5.png'
import p6 from './assets/products/p6.png'
import p7 from './assets/products/p7.png'
import p8 from './assets/products/p8.png'
import p9 from './assets/products/p9.png'
import p10 from './assets/products/p10.png'
import banner from './assets/products/banner.png'

/** 商品名 → 实拍主图映射（未命中返回品牌色兜底） */
const RULES: [RegExp, string][] = [
  [/枸杞青果/, p1],
  [/咖啡/, p2],
  [/薄荷/, p3],
  [/散籽/, p4],
  [/黑金礼盒/, p5],
  [/金玉满堂/, p6],
  [/芝麻/, p7],
  [/话梅/, p8],
  [/木糖醇/, p9],
  [/桂子油/, p10]
]

export function coverFor(name: string, fallback = '#C93A32'): string {
  if (!name) return fallback
  for (const [re, img] of RULES) if (re.test(name)) return img
  return fallback
}

export { banner }
