package com.qingguoji.mall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qingguoji.mall.common.exception.ServiceException;
import com.qingguoji.mall.mall.domain.AfterSale;
import com.qingguoji.mall.mall.domain.MallOrder;
import com.qingguoji.mall.mall.domain.Member;
import com.qingguoji.mall.mall.domain.OrderItem;
import com.qingguoji.mall.mall.domain.Product;
import com.qingguoji.mall.mall.domain.ProductSku;
import com.qingguoji.mall.mall.domain.ScanCode;
import com.qingguoji.mall.mall.domain.dto.AfterSaleBody;
import com.qingguoji.mall.mall.domain.dto.OrderBody;
import com.qingguoji.mall.mall.domain.dto.OrderItemBody;
import com.qingguoji.mall.mall.repository.MemberRepository;
import com.qingguoji.mall.mall.repository.OrderItemRepository;
import com.qingguoji.mall.mall.repository.OrderRepository;
import com.qingguoji.mall.mall.repository.PointsLogRepository;
import com.qingguoji.mall.mall.repository.ProductRepository;
import com.qingguoji.mall.mall.repository.ProductSkuRepository;
import com.qingguoji.mall.mall.repository.ScanCodeRepository;
import com.qingguoji.mall.mall.service.IAfterSaleService;
import com.qingguoji.mall.mall.service.IMemberService;
import com.qingguoji.mall.mall.service.IOrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

/**
 * 集成测试：逐条对应 specs 能力规格的 Requirement/Scenario。
 *
 * <p>运行环境：H2 内存库（独立于运行中的服务，互不影响）。
 *
 * @author qingguoji
 */
@SpringBootTest
class MallFlowIntegrationTest {

    @Autowired IMemberService memberService;
    @Autowired IOrderService orderService;
    @Autowired IAfterSaleService afterSaleService;
    @Autowired MemberRepository memberRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ProductSkuRepository skuRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;
    @Autowired PointsLogRepository pointsLogRepository;
    @Autowired ScanCodeRepository scanCodeRepository;

    // ---------- 测试数据工厂 ----------

    private Member newMember(String phone) {
        return memberService.loginByPhone(phone, "123456");
    }

    /** 建一个商品 + SKU，返回 SKU */
    private ProductSku newSku(String name, String price, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setCategory("测试分类");
        product.setCover("#ccc");
        product.setSales(1);
        product.setStatus(1);
        product.setCreatedAt(LocalDateTime.now());
        product = productRepository.save(product);

        ProductSku sku = new ProductSku();
        sku.setProductId(product.getId());
        sku.setSpec("单包");
        sku.setPrice(new BigDecimal(price));
        sku.setTotalStock(stock);
        sku.setLockedStock(0);
        sku.setSkuCode("T" + UUID.randomUUID());
        return skuRepository.save(sku);
    }

    /** 构造下单请求体 */
    private OrderBody orderBody(ProductSku sku, int qty) {
        OrderBody body = new OrderBody();
        body.setLines(List.of(new OrderItemBody(sku.getId(), qty)));
        body.setReceiver("测试收货人");
        body.setReceiverPhone("13900000000");
        body.setAddress("测试地址");
        return body;
    }

    private MallOrder order(Member member, ProductSku sku, int qty) {
        return orderService.createOrder(member.getId(), orderBody(sku, qty));
    }

    /** 构造售后请求体 */
    private AfterSaleBody afterSaleBody(Long orderItemId, String type, int qty, String reason) {
        AfterSaleBody body = new AfterSaleBody();
        body.setOrderItemId(orderItemId);
        body.setType(type);
        body.setQty(qty);
        body.setReason(reason);
        return body;
    }

    // ---------- 会员积分模块 ----------

    /** Requirement: 手机号验证码登录 —— Scenario: 新用户首次登录 / 验证码错误 */
    @Test
    void tc01_登录注册与验证码校验() {
        assertThrows(
                ServiceException.class,
                () -> memberService.loginByPhone("13911110001", "000000"),
                "验证码错误应拒绝");
        Member member = memberService.loginByPhone("13911110001", "123456");
        assertEquals("注册会员", member.levelName());
        assertEquals(0, member.getPoints());
        Member again = memberService.loginByPhone("13911110001", "123456");
        assertEquals(member.getId(), again.getId(), "重复登录应为同一会员");
    }

    /** Requirement: 每日签到 —— Scenario: 重复签到 */
    @Test
    void tc02_签到积分与重复签到拦截() {
        Member member = newMember("13911110002");
        int before = member.getPoints();
        assertEquals(before + 5, memberService.checkin(member.getId()));
        ServiceException e =
                assertThrows(ServiceException.class, () -> memberService.checkin(member.getId()));
        assertTrue(e.getMessage().contains("今日已签到"));
    }

    /** Requirement: 扫码积分 —— Scenario: 重复使用防伪码 / 超每日上限 */
    @Test
    void tc03_扫码积分_防重与每日上限() {
        Member member = newMember("13911110003");
        ScanCode used = new ScanCode();
        used.setCode("TUSED" + UUID.randomUUID().toString().substring(0, 8));
        used.setUsed(true);
        scanCodeRepository.save(used);
        ServiceException e1 =
                assertThrows(
                        ServiceException.class,
                        () -> memberService.scanCode(member.getId(), used.getCode()));
        assertTrue(e1.getMessage().contains("已被使用"));

        for (int i = 0; i < 20; i++) {
            ScanCode scanCode = new ScanCode();
            scanCode.setCode("TOK" + UUID.randomUUID().toString().substring(0, 10));
            scanCode.setUsed(false);
            scanCodeRepository.save(scanCode);
            memberService.scanCode(member.getId(), scanCode.getCode());
        }
        int after20 = memberRepository.findById(member.getId()).orElseThrow().getPoints();
        assertEquals(200, after20, "20 包 × 10 积分");

        ScanCode extra = new ScanCode();
        extra.setCode("TOK" + UUID.randomUUID().toString().substring(0, 10));
        extra.setUsed(false);
        scanCodeRepository.save(extra);
        ServiceException e2 =
                assertThrows(
                        ServiceException.class,
                        () -> memberService.scanCode(member.getId(), extra.getCode()));
        assertTrue(e2.getMessage().contains("上限"));
    }

    /** Requirement: 五级会员等级体系 —— Scenario: 等级折扣生效（银卡 95 折，倍率 1.5） */
    @Test
    void tc04_等级折扣与积分倍率() {
        Member member = newMember("13911110004");
        member.setGrowth(6000.0);
        memberRepository.save(member);

        ProductSku sku = newSku("倍率测试商品", "100", 100);
        MallOrder order = order(member, sku, 1);
        assertEquals(
                0, order.getDiscountAmount().compareTo(new BigDecimal("5.00")), "银卡 95 折应优惠 5 元");
        assertEquals(
                0,
                order.getPayAmount().compareTo(new BigDecimal("103.00")),
                "100×0.95=95 <99 需加运费 8 → 103");

        orderService.payOrder(order.getOrderNo(), member.getId());
        int points = memberRepository.findById(member.getId()).orElseThrow().getPoints();
        assertEquals(155, points, "实付 103 × 1.5 倍率 = 154.5 → 155");
    }

    // ---------- 商品库存 / 订单模块 ----------

    /** Requirement: 库存四原语 —— Scenario: 库存不足下单（多行整体回滚） */
    @Test
    void tc05_库存不足下单失败并整体回滚() {
        Member member = newMember("13911110005");
        ProductSku ok = newSku("回滚测试A", "10", 100);
        ProductSku lack = newSku("回滚测试B", "10", 3);
        long ordersBefore = orderRepository.count();

        OrderBody body = new OrderBody();
        body.setLines(
                List.of(new OrderItemBody(ok.getId(), 1), new OrderItemBody(lack.getId(), 5)));
        body.setReceiver("张三");
        body.setReceiverPhone("13900000000");
        body.setAddress("地址");

        ServiceException e =
                assertThrows(
                        ServiceException.class,
                        () -> orderService.createOrder(member.getId(), body));
        assertTrue(e.getMessage().contains("库存不足"));
        assertEquals(ordersBefore, orderRepository.count(), "任一行失败不得产生订单");
        assertEquals(
                0, skuRepository.findById(ok.getId()).orElseThrow().getLockedStock(), "已预占行应随事务回滚");
    }

    /** Requirement: 库存四原语 —— Scenario: 并发下单防超卖（AC-01） */
    @Test
    void tc06_并发下单防超卖() throws Exception {
        Member member = newMember("13911110006");
        ProductSku sku = newSku("防超卖商品", "10", 5);
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(
                    () -> {
                        try {
                            start.await();
                            orderService.createOrder(member.getId(), orderBody(sku, 1));
                            success.incrementAndGet();
                        } catch (Exception ignored) {
                            // 库存不足属预期
                        } finally {
                            done.countDown();
                        }
                    });
        }
        start.countDown();
        done.await();
        pool.shutdown();

        assertEquals(5, success.get(), "可售 5 件应恰好成交 5 单");
        ProductSku after = skuRepository.findById(sku.getId()).orElseThrow();
        assertEquals(5, after.getLockedStock());
        assertEquals(0, after.getTotalStock() - after.getLockedStock(), "可售应恰好归零");
    }

    /** Requirement: 模拟支付 —— Scenario: 重复支付（幂等）+ 支付扣减库存发积分 */
    @Test
    void tc07_支付扣减库存发积分与幂等保护() {
        Member member = newMember("13911110007");
        ProductSku sku = newSku("支付测试商品", "20", 100);
        MallOrder order = order(member, sku, 2);

        MallOrder paid = orderService.payOrder(order.getOrderNo(), member.getId());
        assertEquals("PAID", paid.getStatus());
        assertEquals(48, paid.getPointsEarned(), "实付 40 + 8 运费 = 48 × 1.0 倍率");

        ProductSku after = skuRepository.findById(sku.getId()).orElseThrow();
        assertEquals(98, after.getTotalStock());
        assertEquals(0, after.getLockedStock());

        ServiceException e =
                assertThrows(
                        ServiceException.class,
                        () -> orderService.payOrder(order.getOrderNo(), member.getId()));
        assertTrue(e.getMessage().contains("幂等"));
    }

    /** Requirement: 取消与超时取消 —— Scenario: 超时自动取消（AC-02） */
    @Test
    void tc08_超时订单自动取消并释放库存() {
        Member member = newMember("13911110008");
        ProductSku sku = newSku("超时测试商品", "10", 100);
        MallOrder order = order(member, sku, 3);
        assertEquals(3, skuRepository.findById(sku.getId()).orElseThrow().getLockedStock());

        order.setPayDeadline(LocalDateTime.now().minusMinutes(1));
        orderRepository.save(order);

        int canceled = orderService.cancelExpiredOrders();
        assertTrue(canceled >= 1);
        assertEquals("CANCELED", orderRepository.findById(order.getId()).orElseThrow().getStatus());
        assertEquals(
                0, skuRepository.findById(sku.getId()).orElseThrow().getLockedStock(), "库存应释放");

        orderService.cancelExpiredOrders(); // 幂等再跑一次
        assertEquals("CANCELED", orderRepository.findById(order.getId()).orElseThrow().getStatus());
    }

    // ---------- 售后模块 ----------

    /** Requirement: 售后申请约束 —— Scenario: 已发货订单申请仅退款 / 超量售后 */
    @Test
    void tc09_售后申请约束校验() {
        Member member = newMember("13911110009");
        ProductSku sku = newSku("售后约束商品", "10", 100);
        MallOrder order = order(member, sku, 4);
        orderService.payOrder(order.getOrderNo(), member.getId());
        orderService.shipOrder(order.getOrderNo(), "SF999");

        Long itemId = orderItemRepository.findByOrderId(order.getId()).get(0).getId();
        ServiceException e1 =
                assertThrows(
                        ServiceException.class,
                        () ->
                                afterSaleService.applyAfterSale(
                                        member.getId(),
                                        afterSaleBody(itemId, "REFUND_ONLY", 1, "不想要了")));
        assertTrue(e1.getMessage().contains("仅支持退货退款"));

        ServiceException e2 =
                assertThrows(
                        ServiceException.class,
                        () ->
                                afterSaleService.applyAfterSale(
                                        member.getId(),
                                        afterSaleBody(itemId, "RETURN_REFUND", 5, "太多")));
        assertTrue(e2.getMessage().contains("最多可退"));
    }

    /** Requirement: 售后审核流 + 退款完成动作 —— Scenario: 退货退款完整链路（回补库存 + 扣回积分） */
    @Test
    void tc10_退货退款全链路() {
        Member member = newMember("13911110010");
        ProductSku sku = newSku("退货退款商品", "50", 100);
        MallOrder order = order(member, sku, 2); // 100 ≥ 99 免运费，实付 100
        orderService.payOrder(order.getOrderNo(), member.getId());
        assertEquals(100, memberRepository.findById(member.getId()).orElseThrow().getPoints());

        OrderItem item = orderItemRepository.findByOrderId(order.getId()).get(0);
        AfterSale afterSale =
                afterSaleService.applyAfterSale(
                        member.getId(), afterSaleBody(item.getId(), "RETURN_REFUND", 1, "七天无理由"));
        assertEquals("WAIT_AUDIT", afterSale.getStatus());

        afterSale = afterSaleService.audit(afterSale.getId(), true, null);
        assertEquals("WAIT_RETURN", afterSale.getStatus());

        afterSaleService.fillReturnLogistics(afterSale.getId(), member.getId(), "SF001");
        afterSale = afterSaleService.confirmReturn(afterSale.getId());
        assertEquals("DONE", afterSale.getStatus());

        assertEquals(
                99,
                skuRepository.findById(sku.getId()).orElseThrow().getTotalStock(),
                "100 − 支付扣 2 + 退货回补 1 = 99");
        assertEquals(
                50,
                memberRepository.findById(member.getId()).orElseThrow().getPoints(),
                "按退款比例扣回 50 积分");
        assertTrue(
                pointsLogRepository
                        .findByMemberId(member.getId(), PageRequest.of(0, 20))
                        .getContent()
                        .stream()
                        .anyMatch(log -> "REFUND_BACK".equals(log.getType())),
                "应有 REFUND_BACK 负流水");
    }

    /** Requirement: 退款完成动作 —— Scenario: 仅退款完成（不回补库存） */
    @Test
    void tc11_仅退款审核通过即完成并扣回积分() {
        Member member = newMember("13911110011");
        ProductSku sku = newSku("仅退款商品", "10", 100);
        MallOrder order = order(member, sku, 5); // 50 + 8 运费 = 58
        orderService.payOrder(order.getOrderNo(), member.getId());

        OrderItem item = orderItemRepository.findByOrderId(order.getId()).get(0);
        AfterSale afterSale =
                afterSaleService.applyAfterSale(
                        member.getId(), afterSaleBody(item.getId(), "REFUND_ONLY", 1, "拍错了"));
        afterSale = afterSaleService.audit(afterSale.getId(), true, null);

        assertEquals("DONE", afterSale.getStatus());
        assertEquals(
                95,
                skuRepository.findById(sku.getId()).orElseThrow().getTotalStock(),
                "仅退款不回补：100 − 支付扣 5 = 95");
        assertEquals(
                48,
                memberRepository.findById(member.getId()).orElseThrow().getPoints(),
                "扣回 10 积分（58 发放 → 10/58 比例）");
    }

    /** Requirement: 发货与收货状态机 —— Scenario: 状态流转 */
    @Test
    void tc12_发货与确认收货状态机() {
        Member member = newMember("13911110012");
        ProductSku sku = newSku("状态机商品", "10", 100);
        MallOrder order = order(member, sku, 1);

        assertThrows(
                ServiceException.class,
                () -> orderService.shipOrder(order.getOrderNo(), "SF1"),
                "WAIT_PAY 不可发货");
        orderService.payOrder(order.getOrderNo(), member.getId());
        orderService.shipOrder(order.getOrderNo(), "SF123");
        assertEquals("SHIPPED", orderRepository.findById(order.getId()).orElseThrow().getStatus());

        orderService.confirmReceive(order.getOrderNo(), member.getId());
        assertEquals(
                "COMPLETED", orderRepository.findById(order.getId()).orElseThrow().getStatus());
        assertThrows(
                ServiceException.class,
                () -> orderService.confirmReceive(order.getOrderNo(), member.getId()),
                "COMPLETED 不可重复确认");
    }
}
