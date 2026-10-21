package com.qingguoji.mall;

import com.qingguoji.mall.common.BizException;
import com.qingguoji.mall.domain.*;
import com.qingguoji.mall.repo.*;
import com.qingguoji.mall.service.MallService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试：逐条对应 openspec/specs 能力规格的 Requirement/Scenario。
 * 运行环境：H2 内存库（独立于运行中的 8080 服务，互不影响）。
 */
@SpringBootTest
class MallFlowIntegrationTest {

    @Autowired MallService svc;
    @Autowired MemberRepo memberRepo;
    @Autowired ProductRepo productRepo;
    @Autowired ProductSkuRepo skuRepo;
    @Autowired OrderRepo orderRepo;
    @Autowired OrderItemRepo orderItemRepo;
    @Autowired AfterSaleRepo afterSaleRepo;
    @Autowired PointsLogRepo pointsLogRepo;
    @Autowired ScanCodeRepo scanCodeRepo;

    // ---------- 测试数据工厂 ----------

    private Member newMember(String phone) {
        return svc.login(phone, "123456");
    }

    /** 建一个商品+SKU，返回 SKU */
    private ProductSku newSku(String name, String price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setCategory("测试分类");
        p.setCover("#ccc");
        p.setSales(1);
        p.setStatus(1);
        p.setCreatedAt(LocalDateTime.now());
        p = productRepo.save(p);
        ProductSku sku = new ProductSku();
        sku.setProductId(p.getId());
        sku.setSpec("单包");
        sku.setPrice(new BigDecimal(price));
        sku.setTotalStock(stock);
        sku.setLockedStock(0);
        sku.setSkuCode("T" + UUID.randomUUID());
        return skuRepo.save(sku);
    }

    private MallOrder order(Member m, ProductSku sku, int qty) {
        return svc.createOrder(m.getId(), List.of(new MallService.OrderReqLine(sku.getId(), qty)),
                "测试收货人", "13900000000", "测试地址");
    }

    // ---------- 会员积分模块 ----------

    /** Requirement: 手机号验证码登录 —— Scenario: 新用户首次登录 / 验证码错误 */
    @Test
    void TC01_登录注册与验证码校验() {
        assertThrows(BizException.class, () -> svc.login("13911110001", "000000"), "验证码错误应拒绝");
        Member m = svc.login("13911110001", "123456");
        assertEquals("注册会员", m.levelName());
        assertEquals(0, m.getPoints());
        Member again = svc.login("13911110001", "123456");
        assertEquals(m.getId(), again.getId(), "重复登录应为同一会员");
    }

    /** Requirement: 每日签到 —— Scenario: 重复签到 */
    @Test
    void TC02_签到积分与重复签到拦截() {
        Member m = newMember("13911110002");
        int before = m.getPoints();
        assertEquals(before + 5, svc.checkin(m.getId()));
        BizException e = assertThrows(BizException.class, () -> svc.checkin(m.getId()));
        assertTrue(e.getMessage().contains("今日已签到"));
    }

    /** Requirement: 扫码积分 —— Scenario: 重复使用防伪码 / 超每日上限 */
    @Test
    void TC03_扫码积分_防重与每日上限() {
        Member m = newMember("13911110003");
        ScanCode used = new ScanCode();
        used.setCode("TUSED" + UUID.randomUUID().toString().substring(0, 8));
        used.setUsed(true);
        scanCodeRepo.save(used);
        BizException e1 = assertThrows(BizException.class, () -> svc.scanCode(m.getId(), used.getCode()));
        assertTrue(e1.getMessage().contains("已被使用"));

        for (int i = 0; i < 20; i++) {
            ScanCode sc = new ScanCode();
            sc.setCode("TOK" + UUID.randomUUID().toString().substring(0, 10));
            sc.setUsed(false);
            scanCodeRepo.save(sc);
            svc.scanCode(m.getId(), sc.getCode());
        }
        int after20 = memberRepo.findById(m.getId()).orElseThrow().getPoints();
        assertEquals(200, after20, "20 包 × 10 积分");
        ScanCode extra = new ScanCode();
        extra.setCode("TOK" + UUID.randomUUID().toString().substring(0, 10));
        extra.setUsed(false);
        scanCodeRepo.save(extra);
        BizException e2 = assertThrows(BizException.class, () -> svc.scanCode(m.getId(), extra.getCode()));
        assertTrue(e2.getMessage().contains("上限"));
    }

    /** Requirement: 五级会员等级体系 —— Scenario: 等级折扣生效（银卡 95 折，倍率 1.5） */
    @Test
    void TC04_等级折扣与积分倍率() {
        Member m = newMember("13911110004");
        m.setGrowth(6000.0);
        memberRepo.save(m);
        ProductSku sku = newSku("倍率测试商品", "100", 100);
        MallOrder o = order(m, sku, 1);
        assertEquals(0, o.getDiscountAmount().compareTo(new BigDecimal("5.00")), "银卡 95 折应优惠 5 元");
        assertEquals(0, o.getPayAmount().compareTo(new BigDecimal("103.00")), "100×0.95=95 <99 需加运费 8 → 103");
        svc.pay(o.getOrderNo(), m.getId());
        int points = memberRepo.findById(m.getId()).orElseThrow().getPoints();
        assertEquals(155, points, "实付 103 × 1.5 倍率 = 154.5 → 155");
    }

    // ---------- 商品库存 / 订单模块 ----------

    /** Requirement: 库存四原语 —— Scenario: 库存不足下单（多行整体回滚） */
    @Test
    void TC05_库存不足下单失败并整体回滚() {
        Member m = newMember("13911110005");
        ProductSku ok = newSku("回滚测试A", "10", 100);
        ProductSku lack = newSku("回滚测试B", "10", 3);
        long ordersBefore = orderRepo.count();
        BizException e = assertThrows(BizException.class, () -> svc.createOrder(m.getId(),
                List.of(new MallService.OrderReqLine(ok.getId(), 1), new MallService.OrderReqLine(lack.getId(), 5)),
                "张三", "13900000000", "地址"));
        assertTrue(e.getMessage().contains("库存不足"));
        assertEquals(ordersBefore, orderRepo.count(), "任一行失败不得产生订单");
        assertEquals(0, skuRepo.findById(ok.getId()).orElseThrow().getLockedStock(), "已预占行应随事务回滚");
    }

    /** Requirement: 库存四原语 —— Scenario: 并发下单防超卖（AC-01） */
    @Test
    void TC06_并发下单防超卖() throws Exception {
        Member m = newMember("13911110006");
        ProductSku sku = newSku("防超卖商品", "10", 5);
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    svc.createOrder(m.getId(), List.of(new MallService.OrderReqLine(sku.getId(), 1)),
                            "并发", "13900000000", "地址");
                    success.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdown();
        assertEquals(5, success.get(), "可售 5 件应恰好成交 5 单");
        ProductSku after = skuRepo.findById(sku.getId()).orElseThrow();
        assertEquals(5, after.getLockedStock());
        assertEquals(0, after.getTotalStock() - after.getLockedStock(), "可售应恰好归零");
    }

    /** Requirement: 模拟支付 —— Scenario: 重复支付（幂等）+ 支付扣减库存发积分 */
    @Test
    void TC07_支付扣减库存发积分与幂等保护() {
        Member m = newMember("13911110007");
        ProductSku sku = newSku("支付测试商品", "20", 100);
        MallOrder o = order(m, sku, 2);
        MallOrder paid = svc.pay(o.getOrderNo(), m.getId());
        assertEquals("PAID", paid.getStatus());
        assertEquals(48, paid.getPointsEarned(), "实付 40+8 运费 = 48 × 1.0 倍率");
        ProductSku after = skuRepo.findById(sku.getId()).orElseThrow();
        assertEquals(98, after.getTotalStock());
        assertEquals(0, after.getLockedStock());
        BizException e = assertThrows(BizException.class, () -> svc.pay(o.getOrderNo(), m.getId()));
        assertTrue(e.getMessage().contains("幂等"));
    }

    /** Requirement: 取消与超时取消 —— Scenario: 超时自动取消（AC-02） */
    @Test
    void TC08_超时订单自动取消并释放库存() {
        Member m = newMember("13911110008");
        ProductSku sku = newSku("超时测试商品", "10", 100);
        MallOrder o = order(m, sku, 3);
        assertEquals(3, skuRepo.findById(sku.getId()).orElseThrow().getLockedStock());
        o.setPayDeadline(LocalDateTime.now().minusMinutes(1));
        orderRepo.save(o);
        int canceled = svc.cancelExpiredOrders();
        assertTrue(canceled >= 1);
        assertEquals("CANCELED", orderRepo.findById(o.getId()).orElseThrow().getStatus());
        assertEquals(0, skuRepo.findById(sku.getId()).orElseThrow().getLockedStock(), "库存应释放");
        svc.cancelExpiredOrders(); // 幂等再跑一次
        assertEquals("CANCELED", orderRepo.findById(o.getId()).orElseThrow().getStatus());
    }

    // ---------- 售后模块 ----------

    /** Requirement: 售后申请约束 —— Scenario: 已发货订单申请仅退款 / 超量售后 */
    @Test
    void TC09_售后申请约束校验() {
        Member m = newMember("13911110009");
        ProductSku sku = newSku("售后约束商品", "10", 100);
        MallOrder o = order(m, sku, 4);
        svc.pay(o.getOrderNo(), m.getId());
        svc.ship(o.getOrderNo(), "SF999");
        List<OrderItem> items = orderItemRepo.findByOrderId(o.getId());
        Long itemId = items.get(0).getId();
        BizException e1 = assertThrows(BizException.class,
                () -> svc.applyAfterSale(m.getId(), itemId, "REFUND_ONLY", 1, "不想要了", ""));
        assertTrue(e1.getMessage().contains("仅支持退货退款"));
        BizException e2 = assertThrows(BizException.class,
                () -> svc.applyAfterSale(m.getId(), itemId, "RETURN_REFUND", 5, "太多", ""));
        assertTrue(e2.getMessage().contains("最多可退"));
    }

    /** Requirement: 售后审核流 + 退款完成动作 —— Scenario: 退货退款完整链路（回补库存+扣回积分） */
    @Test
    void TC10_退货退款全链路() {
        Member m = newMember("13911110010");
        ProductSku sku = newSku("退货退款商品", "50", 100);
        MallOrder o = order(m, sku, 2); // 100 ≥99 免运费，实付 100
        svc.pay(o.getOrderNo(), m.getId());
        int pointsAfterPay = memberRepo.findById(m.getId()).orElseThrow().getPoints();
        assertEquals(100, pointsAfterPay);

        OrderItem item = orderItemRepo.findByOrderId(o.getId()).get(0);
        AfterSale as = svc.applyAfterSale(m.getId(), item.getId(), "RETURN_REFUND", 1, "七天无理由", "");
        assertEquals("WAIT_AUDIT", as.getStatus());

        as = svc.audit(as.getId(), true, null);
        assertEquals("WAIT_RETURN", as.getStatus());

        svc.fillReturnLogistics(as.getId(), m.getId(), "SF001");
        as = svc.confirmReturn(as.getId());
        assertEquals("DONE", as.getStatus());

        assertEquals(99, skuRepo.findById(sku.getId()).orElseThrow().getTotalStock(), "100 − 支付扣2 + 退货回补1 = 99");
        int pointsAfterRefund = memberRepo.findById(m.getId()).orElseThrow().getPoints();
        assertEquals(50, pointsAfterRefund, "按退款比例扣回 50 积分");
        assertTrue(pointsLogRepo.findByMemberId(m.getId(), org.springframework.data.domain.PageRequest.of(0, 20))
                .getContent().stream().anyMatch(l -> "REFUND_BACK".equals(l.getType())), "应有 REFUND_BACK 负流水");
    }

    /** Requirement: 退款完成动作 —— Scenario: 仅退款完成（不回补库存） */
    @Test
    void TC11_仅退款审核通过即完成并扣回积分() {
        Member m = newMember("13911110011");
        ProductSku sku = newSku("仅退款商品", "10", 100);
        MallOrder o = order(m, sku, 5); // 50 + 8 运费 = 58
        svc.pay(o.getOrderNo(), m.getId());
        OrderItem item = orderItemRepo.findByOrderId(o.getId()).get(0);
        AfterSale as = svc.applyAfterSale(m.getId(), item.getId(), "REFUND_ONLY", 1, "拍错了", "");
        as = svc.audit(as.getId(), true, null);
        assertEquals("DONE", as.getStatus());
        assertEquals(95, skuRepo.findById(sku.getId()).orElseThrow().getTotalStock(), "仅退款不回补：100 − 支付扣5 = 95");
        int points = memberRepo.findById(m.getId()).orElseThrow().getPoints();
        assertEquals(48, points, "扣回 10 积分（58 发放 → 10/58 比例）");
    }

    /** Requirement: 发货与收货状态机 —— Scenario: 状态流转 */
    @Test
    void TC12_发货与确认收货状态机() {
        Member m = newMember("13911110012");
        ProductSku sku = newSku("状态机商品", "10", 100);
        MallOrder o = order(m, sku, 1);
        assertThrows(BizException.class, () -> svc.ship(o.getOrderNo(), "SF1"), "WAIT_PAY 不可发货");
        svc.pay(o.getOrderNo(), m.getId());
        svc.ship(o.getOrderNo(), "SF123");
        assertEquals("SHIPPED", orderRepo.findById(o.getId()).orElseThrow().getStatus());
        svc.confirmReceive(o.getOrderNo(), m.getId());
        assertEquals("COMPLETED", orderRepo.findById(o.getId()).orElseThrow().getStatus());
        assertThrows(BizException.class, () -> svc.confirmReceive(o.getOrderNo(), m.getId()), "COMPLETED 不可重复确认");
    }
}
