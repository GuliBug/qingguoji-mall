package com.qingguoji.mall;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qingguoji.mall.common.exception.ServiceException;
import com.qingguoji.mall.mall.domain.CartItem;
import com.qingguoji.mall.mall.domain.MallOrder;
import com.qingguoji.mall.mall.domain.Member;
import com.qingguoji.mall.mall.domain.OrderItem;
import com.qingguoji.mall.mall.domain.Product;
import com.qingguoji.mall.mall.domain.ProductSku;
import com.qingguoji.mall.mall.domain.dto.AfterSaleBody;
import com.qingguoji.mall.mall.domain.dto.CartAddBody;
import com.qingguoji.mall.mall.domain.dto.CartUpdateBody;
import com.qingguoji.mall.mall.domain.dto.OrderBody;
import com.qingguoji.mall.mall.domain.dto.OrderItemBody;
import com.qingguoji.mall.mall.repository.CartRepository;
import com.qingguoji.mall.mall.repository.MemberRepository;
import com.qingguoji.mall.mall.repository.OrderRepository;
import com.qingguoji.mall.mall.repository.ProductRepository;
import com.qingguoji.mall.mall.repository.ProductSkuRepository;
import com.qingguoji.mall.mall.service.IAdminService;
import com.qingguoji.mall.mall.service.IAfterSaleService;
import com.qingguoji.mall.mall.service.ICartService;
import com.qingguoji.mall.mall.service.IMemberService;
import com.qingguoji.mall.mall.service.IOrderService;
import com.qingguoji.mall.mall.service.IProductService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 字段级校验测试：每个接口的每个字段（空值 / 格式 / 边界 / 非法值）逐一验证。
 *
 * @author qingguoji
 */
@SpringBootTest
class FieldValidationTest {

    @Autowired IMemberService memberService;
    @Autowired IProductService productService;
    @Autowired ICartService cartService;
    @Autowired IOrderService orderService;
    @Autowired IAfterSaleService afterSaleService;
    @Autowired IAdminService adminService;
    @Autowired ProductRepository productRepository;
    @Autowired ProductSkuRepository skuRepository;
    @Autowired CartRepository cartRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired MemberRepository memberRepository;

    private Member newMember() {
        return memberService.loginByPhone(
                "139" + String.valueOf(System.nanoTime()).substring(0, 8), "123456");
    }

    private ProductSku newSku(String price, int stock) {
        Product product = new Product();
        product.setName("字段校验商品" + UUID.randomUUID().toString().substring(0, 6));
        product.setCategory("测试");
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
        sku.setSkuCode("FV" + UUID.randomUUID());
        return skuRepository.save(sku);
    }

    private OrderBody orderBody(
            List<OrderItemBody> lines, String receiver, String phone, String address) {
        OrderBody body = new OrderBody();
        body.setLines(lines);
        body.setReceiver(receiver);
        body.setReceiverPhone(phone);
        body.setAddress(address);
        return body;
    }

    private AfterSaleBody afterSaleBody(Long itemId, String type, Integer qty, String reason) {
        AfterSaleBody body = new AfterSaleBody();
        body.setOrderItemId(itemId);
        body.setType(type);
        body.setQty(qty);
        body.setReason(reason);
        body.setDescription("");
        return body;
    }

    private CartAddBody cartAddBody(Long skuId, Integer qty) {
        CartAddBody body = new CartAddBody();
        body.setSkuId(skuId);
        body.setQty(qty);
        return body;
    }

    private CartUpdateBody cartUpdateBody(Integer qty) {
        CartUpdateBody body = new CartUpdateBody();
        body.setQty(qty);
        return body;
    }

    // ---------- 登录接口：phone / smsCode ----------

    @Test
    void f01_登录_phone字段() {
        ServiceException e1 =
                assertThrows(
                        ServiceException.class, () -> memberService.loginByPhone(null, "123456"));
        assertTrue(e1.getMessage().contains("手机号格式"));
        assertTrue(
                assertThrows(ServiceException.class, () -> memberService.loginByPhone("", "123456"))
                        .getMessage()
                        .contains("手机号格式"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> memberService.loginByPhone("123", "123456"))
                        .getMessage()
                        .contains("手机号格式"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> memberService.loginByPhone("23800001111", "123456"))
                        .getMessage()
                        .contains("手机号格式"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> memberService.loginByPhone("1380000111", "123456"))
                        .getMessage()
                        .contains("手机号格式"));
    }

    @Test
    void f02_登录_smsCode字段() {
        String phone = "139" + String.valueOf(System.nanoTime()).substring(0, 8);
        assertTrue(
                assertThrows(ServiceException.class, () -> memberService.loginByPhone(phone, null))
                        .getMessage()
                        .contains("验证码不能为空"));
        assertTrue(
                assertThrows(ServiceException.class, () -> memberService.loginByPhone(phone, "  "))
                        .getMessage()
                        .contains("验证码不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> memberService.loginByPhone(phone, "654321"))
                        .getMessage()
                        .contains("验证码错误"));
    }

    // ---------- 加购接口：skuId / qty ----------

    @Test
    void f03_加购_skuId字段() {
        Member member = newMember();
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> cartService.addCart(member.getId(), null, 1))
                        .getMessage()
                        .contains("SKU 不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> cartService.addCart(member.getId(), 999999L, 1))
                        .getMessage()
                        .contains("SKU 不存在"));
    }

    @Test
    void f04_加购_qty字段() {
        Member member = newMember();
        ProductSku sku = newSku("10", 100);
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> cartService.addCart(member.getId(), sku.getId(), null))
                        .getMessage()
                        .contains("大于 0"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> cartService.addCart(member.getId(), sku.getId(), 0))
                        .getMessage()
                        .contains("大于 0"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> cartService.addCart(member.getId(), sku.getId(), -1))
                        .getMessage()
                        .contains("大于 0"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> cartService.addCart(member.getId(), sku.getId(), 1000))
                        .getMessage()
                        .contains("999"));
        cartService.addCart(member.getId(), sku.getId(), 1); // 边界内正常
        cartService.addCart(member.getId(), sku.getId(), 999); // 上边界 999 正常
    }

    // ---------- 购物车更新接口：cartId / qty ----------

    @Test
    void f05_购物车更新_字段() {
        Member member = newMember();
        ProductSku sku = newSku("10", 100);
        cartService.addCart(member.getId(), sku.getId(), 2);
        CartItem cartItem = cartRepository.findByMemberId(member.getId()).get(0);

        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        cartService.updateCart(
                                                member.getId(), cartItem.getId(), null))
                        .getMessage()
                        .contains("数量不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> cartService.updateCart(member.getId(), 999999L, 1))
                        .getMessage()
                        .contains("购物车项不存在"));

        cartService.updateCart(member.getId(), cartItem.getId(), 0); // 边界：0 即删除
        assertTrue(cartRepository.findByMemberId(member.getId()).isEmpty());
    }

    // ---------- 下单接口：lines / skuId / qty / receiver / phone / address ----------

    @Test
    void f06_下单_收件人三字段() {
        Member member = newMember();
        ProductSku sku = newSku("10", 100);
        List<OrderItemBody> lines = List.of(new OrderItemBody(sku.getId(), 1));

        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(lines, null, "13800001111", "地址")))
                        .getMessage()
                        .contains("收货人不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(lines, "  ", "13800001111", "地址")))
                        .getMessage()
                        .contains("收货人不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(), orderBody(lines, "张三", null, "地址")))
                        .getMessage()
                        .contains("手机号格式"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(lines, "张三", "12345", "地址")))
                        .getMessage()
                        .contains("手机号格式"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(lines, "张三", "13800001111", "")))
                        .getMessage()
                        .contains("收货地址不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(lines, "张三", "13800001111", null)))
                        .getMessage()
                        .contains("收货地址不能为空"));
    }

    @Test
    void f07_下单_lines与明细字段() {
        Member member = newMember();
        ProductSku sku = newSku("10", 100);

        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(null, "张三", "13800001111", "地址")))
                        .getMessage()
                        .contains("明细不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(List.of(), "张三", "13800001111", "地址")))
                        .getMessage()
                        .contains("明细不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(
                                                        List.of(new OrderItemBody(null, 1)),
                                                        "张三",
                                                        "13800001111",
                                                        "地址")))
                        .getMessage()
                        .contains("SKU 不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(
                                                        List.of(new OrderItemBody(sku.getId(), 0)),
                                                        "张三",
                                                        "13800001111",
                                                        "地址")))
                        .getMessage()
                        .contains("大于 0"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(
                                                        List.of(new OrderItemBody(sku.getId(), -2)),
                                                        "张三",
                                                        "13800001111",
                                                        "地址")))
                        .getMessage()
                        .contains("大于 0"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(
                                                        List.of(new OrderItemBody(999999L, 1)),
                                                        "张三",
                                                        "13800001111",
                                                        "地址")))
                        .getMessage()
                        .contains("SKU 不存在"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        orderService.createOrder(
                                                member.getId(),
                                                orderBody(
                                                        List.of(
                                                                new OrderItemBody(
                                                                        sku.getId(), 101)),
                                                        "张三",
                                                        "13800001111",
                                                        "地址")))
                        .getMessage()
                        .contains("库存不足"));
    }

    // ---------- 扫码接口：code ----------

    @Test
    void f08_扫码_code字段() {
        Member member = newMember();
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> memberService.scanCode(member.getId(), null))
                        .getMessage()
                        .contains("不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> memberService.scanCode(member.getId(), ""))
                        .getMessage()
                        .contains("不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> memberService.scanCode(member.getId(), "NOT-EXIST"))
                        .getMessage()
                        .contains("不存在"));
    }

    // ---------- 售后接口：orderItemId / type / qty / reason ----------

    @Test
    void f09_售后_字段() {
        Member member = newMember();
        ProductSku sku = newSku("10", 100);
        MallOrder order =
                orderService.createOrder(
                        member.getId(),
                        orderBody(
                                List.of(new OrderItemBody(sku.getId(), 2)),
                                "张三",
                                "13800001111",
                                "地址"));
        orderService.payOrder(order.getOrderNo(), member.getId());
        Long itemId =
                orderRepository
                        .findByOrderNo(order.getOrderNo())
                        .orElseThrow()
                        .getItems()
                        .get(0)
                        .getId();

        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        afterSaleService.applyAfterSale(
                                                member.getId(),
                                                afterSaleBody(itemId, "EXCHANGE", 1, "原因")))
                        .getMessage()
                        .contains("售后类型不合法"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        afterSaleService.applyAfterSale(
                                                member.getId(),
                                                afterSaleBody(itemId, null, 1, "原因")))
                        .getMessage()
                        .contains("售后类型不合法"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        afterSaleService.applyAfterSale(
                                                member.getId(),
                                                afterSaleBody(itemId, "REFUND_ONLY", 1, "")))
                        .getMessage()
                        .contains("售后原因"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        afterSaleService.applyAfterSale(
                                                member.getId(),
                                                afterSaleBody(itemId, "REFUND_ONLY", 1, null)))
                        .getMessage()
                        .contains("售后原因"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () ->
                                        afterSaleService.applyAfterSale(
                                                member.getId(),
                                                afterSaleBody(999999L, "REFUND_ONLY", 1, "原因")))
                        .getMessage()
                        .contains("订单明细不存在"));
    }

    // ---------- 管理端：status / delta / logisticsNo / 登录 ----------

    @Test
    void f10_管理端_商品状态与库存调整字段() {
        ProductSku sku = newSku("10", 100);
        Product product = productRepository.findById(sku.getProductId()).orElseThrow();

        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> productService.changeProductStatus(product.getId(), null))
                        .getMessage()
                        .contains("状态值不合法"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> productService.changeProductStatus(product.getId(), 2))
                        .getMessage()
                        .contains("状态值不合法"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> productService.changeProductStatus(product.getId(), -1))
                        .getMessage()
                        .contains("状态值不合法"));
        assertDoesNotThrow(() -> productService.changeProductStatus(product.getId(), 0)); // 合法下架
        assertDoesNotThrow(() -> productService.changeProductStatus(product.getId(), 1)); // 合法上架

        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> productService.adjustStock(sku.getId(), null, null))
                        .getMessage()
                        .contains("不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> productService.adjustStock(sku.getId(), 0, null))
                        .getMessage()
                        .contains("不能为 0"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> productService.adjustStock(sku.getId(), -999, null))
                        .getMessage()
                        .contains("库存不足"));

        Map<String, Object> result = productService.adjustStock(sku.getId(), 50, "补货");
        assertEquals(150, result.get("totalStock")); // 100 + 50，边界内正常
    }

    @Test
    void f11_管理端_积分调整与发货字段() {
        Member member = newMember();
        int before = member.getPoints();

        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> memberService.adjustPoints(member.getId(), null, null))
                        .getMessage()
                        .contains("不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> memberService.adjustPoints(member.getId(), 0, null))
                        .getMessage()
                        .contains("不能为 0"));
        assertEquals(before + 10, memberService.adjustPoints(member.getId(), 10, "活动奖励")); // 合法 +10
        assertEquals(before, memberService.adjustPoints(member.getId(), -10, "撤销奖励")); // 负向扣减合法

        ProductSku sku = newSku("10", 100);
        MallOrder order =
                orderService.createOrder(
                        member.getId(),
                        orderBody(
                                List.of(new OrderItemBody(sku.getId(), 1)),
                                "张三",
                                "13800001111",
                                "地址"));
        orderService.payOrder(order.getOrderNo(), member.getId());

        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> orderService.shipOrder(order.getOrderNo(), ""))
                        .getMessage()
                        .contains("物流单号不能为空"));
        assertTrue(
                assertThrows(
                                ServiceException.class,
                                () -> orderService.shipOrder(order.getOrderNo(), null))
                        .getMessage()
                        .contains("物流单号不能为空"));

        orderService.shipOrder(order.getOrderNo(), "SF-FV-001"); // 合法发货
        assertEquals(
                "SHIPPED",
                orderRepository.findByOrderNo(order.getOrderNo()).orElseThrow().getStatus());
    }

    @Test
    void f12_管理端_登录字段() {
        assertNull(adminService.login("admin", "wrong"), "错误密码应返回 null");
        assertNull(adminService.login("hacker", "admin123"), "错误用户名应返回 null");
        assertNotNull(adminService.login("admin", "admin123"), "正确账号应签发 token");
    }

    @Test
    void f13_管理端_看板指标齐全() {
        Map<String, Object> dashboard = adminService.selectDashboard();
        assertTrue(dashboard.containsKey("waitShipOrders"));
        assertTrue(dashboard.containsKey("waitAuditAfterSales"));
        assertTrue(dashboard.containsKey("memberCount"));
        assertTrue(dashboard.containsKey("lowStockSkuCount"));
    }

    /** 售后明细行 ID 便于后续扩展 */
    @SuppressWarnings("unused")
    private Long firstItemId(MallOrder order) {
        List<OrderItem> items = order.getItems();
        return items == null || items.isEmpty() ? null : items.get(0).getId();
    }

    /** 会员积分查询（保留断言便利性） */
    @SuppressWarnings("unused")
    private int pointsOf(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow().getPoints();
    }
}
