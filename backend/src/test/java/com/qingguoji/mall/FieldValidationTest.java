package com.qingguoji.mall;

import com.qingguoji.mall.common.BizException;
import com.qingguoji.mall.common.R;
import com.qingguoji.mall.common.TokenStore;
import com.qingguoji.mall.controller.AdminController;
import com.qingguoji.mall.domain.*;
import com.qingguoji.mall.repo.*;
import com.qingguoji.mall.service.MallService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字段级校验测试：每个接口的每个字段（空值/格式/边界/非法值）逐一验证。
 */
@SpringBootTest
class FieldValidationTest {

    @Autowired MallService svc;
    @Autowired AdminController adminController;
    @Autowired TokenStore tokenStore;
    @Autowired ProductRepo productRepo;
    @Autowired ProductSkuRepo skuRepo;
    @Autowired CartRepo cartRepo;
    @Autowired OrderRepo orderRepo;
    @Autowired ScanCodeRepo scanCodeRepo;

    private Member newMember() {
        return svc.login("139" + String.valueOf(System.nanoTime()).substring(0, 8), "123456");
    }

    private ProductSku newSku(String price, int stock) {
        Product p = new Product();
        p.setName("字段校验商品" + UUID.randomUUID().toString().substring(0, 6));
        p.setCategory("测试");
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
        sku.setSkuCode("FV" + UUID.randomUUID());
        return skuRepo.save(sku);
    }

    // ---------- 登录接口：phone / smsCode ----------

    @Test
    void F01_登录_phone字段() {
        BizException e1 = assertThrows(BizException.class, () -> svc.login(null, "123456"));
        assertTrue(e1.getMessage().contains("手机号格式"));
        assertTrue(assertThrows(BizException.class, () -> svc.login("", "123456")).getMessage().contains("手机号格式"));
        assertTrue(assertThrows(BizException.class, () -> svc.login("123", "123456")).getMessage().contains("手机号格式"));
        assertTrue(assertThrows(BizException.class, () -> svc.login("23800001111", "123456")).getMessage().contains("手机号格式"));
        assertTrue(assertThrows(BizException.class, () -> svc.login("1380000111", "123456")).getMessage().contains("手机号格式"));
    }

    @Test
    void F02_登录_smsCode字段() {
        String phone = "139" + String.valueOf(System.nanoTime()).substring(0, 8);
        assertTrue(assertThrows(BizException.class, () -> svc.login(phone, null)).getMessage().contains("验证码不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.login(phone, "  ")).getMessage().contains("验证码不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.login(phone, "654321")).getMessage().contains("验证码错误"));
    }

    // ---------- 加购接口：skuId / qty ----------

    @Test
    void F03_加购_skuId字段() {
        Member m = newMember();
        assertTrue(assertThrows(BizException.class, () -> svc.addToCart(m.getId(), null, 1)).getMessage().contains("SKU 不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.addToCart(m.getId(), 999999L, 1)).getMessage().contains("SKU 不存在"));
    }

    @Test
    void F04_加购_qty字段() {
        Member m = newMember();
        ProductSku sku = newSku("10", 100);
        assertTrue(assertThrows(BizException.class, () -> svc.addToCart(m.getId(), sku.getId(), null)).getMessage().contains("大于 0"));
        assertTrue(assertThrows(BizException.class, () -> svc.addToCart(m.getId(), sku.getId(), 0)).getMessage().contains("大于 0"));
        assertTrue(assertThrows(BizException.class, () -> svc.addToCart(m.getId(), sku.getId(), -1)).getMessage().contains("大于 0"));
        assertTrue(assertThrows(BizException.class, () -> svc.addToCart(m.getId(), sku.getId(), 1000)).getMessage().contains("999"));
        svc.addToCart(m.getId(), sku.getId(), 1); // 边界内正常
        svc.addToCart(m.getId(), sku.getId(), 999); // 上边界 999 正常
    }

    // ---------- 购物车更新接口：cartId / qty ----------

    @Test
    void F05_购物车更新_字段() {
        Member m = newMember();
        ProductSku sku = newSku("10", 100);
        svc.addToCart(m.getId(), sku.getId(), 2);
        CartItem ci = cartRepo.findByMemberId(m.getId()).get(0);
        assertTrue(assertThrows(BizException.class, () -> svc.updateCart(m.getId(), ci.getId(), null)).getMessage().contains("数量不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.updateCart(m.getId(), 999999L, 1)).getMessage().contains("购物车项不存在"));
        svc.updateCart(m.getId(), ci.getId(), 0); // 边界：0 即删除
        assertTrue(cartRepo.findByMemberId(m.getId()).isEmpty());
    }

    // ---------- 下单接口：lines / skuId / qty / receiver / phone / address ----------

    @Test
    void F06_下单_收件人三字段() {
        Member m = newMember();
        ProductSku sku = newSku("10", 100);
        var lines = List.of(new MallService.OrderReqLine(sku.getId(), 1));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(), lines, null, "13800001111", "地址")).getMessage().contains("收货人不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(), lines, "  ", "13800001111", "地址")).getMessage().contains("收货人不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(), lines, "张三", null, "地址")).getMessage().contains("手机号格式"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(), lines, "张三", "12345", "地址")).getMessage().contains("手机号格式"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(), lines, "张三", "13800001111", "")).getMessage().contains("收货地址不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(), lines, "张三", "13800001111", null)).getMessage().contains("收货地址不能为空"));
    }

    @Test
    void F07_下单_lines与明细字段() {
        Member m = newMember();
        ProductSku sku = newSku("10", 100);
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(), null, "张三", "13800001111", "地址")).getMessage().contains("明细不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(), List.of(), "张三", "13800001111", "地址")).getMessage().contains("明细不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(),
                List.of(new MallService.OrderReqLine(null, 1)), "张三", "13800001111", "地址")).getMessage().contains("SKU 不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(),
                List.of(new MallService.OrderReqLine(sku.getId(), 0)), "张三", "13800001111", "地址")).getMessage().contains("大于 0"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(),
                List.of(new MallService.OrderReqLine(sku.getId(), -2)), "张三", "13800001111", "地址")).getMessage().contains("大于 0"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(),
                List.of(new MallService.OrderReqLine(999999L, 1)), "张三", "13800001111", "地址")).getMessage().contains("SKU 不存在"));
        assertTrue(assertThrows(BizException.class, () -> svc.createOrder(m.getId(),
                List.of(new MallService.OrderReqLine(sku.getId(), 101)), "张三", "13800001111", "地址")).getMessage().contains("库存不足"));
    }

    // ---------- 扫码接口：code ----------

    @Test
    void F08_扫码_code字段() {
        Member m = newMember();
        assertTrue(assertThrows(BizException.class, () -> svc.scanCode(m.getId(), null)).getMessage().contains("不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.scanCode(m.getId(), "")).getMessage().contains("不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.scanCode(m.getId(), "NOT-EXIST")).getMessage().contains("不存在"));
    }

    // ---------- 售后接口：orderItemId / type / qty / reason ----------

    @Test
    void F09_售后_字段() {
        Member m = newMember();
        ProductSku sku = newSku("10", 100);
        MallOrder o = svc.createOrder(m.getId(), List.of(new MallService.OrderReqLine(sku.getId(), 2)), "张三", "13800001111", "地址");
        svc.pay(o.getOrderNo(), m.getId());
        var item = orderRepo.findByOrderNo(o.getOrderNo()).orElseThrow();
        Long itemId = item.getItems().get(0).getId();
        assertTrue(assertThrows(BizException.class, () -> svc.applyAfterSale(m.getId(), itemId, "EXCHANGE", 1, "原因", "")).getMessage().contains("售后类型不合法"));
        assertTrue(assertThrows(BizException.class, () -> svc.applyAfterSale(m.getId(), itemId, null, 1, "原因", "")).getMessage().contains("售后类型不合法"));
        assertTrue(assertThrows(BizException.class, () -> svc.applyAfterSale(m.getId(), itemId, "REFUND_ONLY", 1, "", "")).getMessage().contains("售后原因"));
        assertTrue(assertThrows(BizException.class, () -> svc.applyAfterSale(m.getId(), itemId, "REFUND_ONLY", 1, null, "")).getMessage().contains("售后原因"));
        assertTrue(assertThrows(BizException.class, () -> svc.applyAfterSale(m.getId(), 999999L, "REFUND_ONLY", 1, "原因", "")).getMessage().contains("订单明细不存在"));
    }

    // ---------- 管理端：status / delta / logisticsNo / 登录 ----------

    @Test
    void F10_管理端_商品状态与库存调整字段() {
        String adminToken = tokenStore.adminLogin("admin", "admin123");
        ProductSku sku = newSku("10", 100);
        Product p = productRepo.findById(sku.getProductId()).orElseThrow();

        Map<String, Integer> badStatus = new HashMap<>();
        badStatus.put("status", null);
        assertTrue(assertThrows(BizException.class, () -> adminController.changeStatus(adminToken, p.getId(), badStatus)).getMessage().contains("状态值不合法"));
        assertTrue(assertThrows(BizException.class, () -> adminController.changeStatus(adminToken, p.getId(), Map.of("status", 2))).getMessage().contains("状态值不合法"));
        assertTrue(assertThrows(BizException.class, () -> adminController.changeStatus(adminToken, p.getId(), Map.of("status", -1))).getMessage().contains("状态值不合法"));
        assertEquals(200, adminController.changeStatus(adminToken, p.getId(), Map.of("status", 0)).getCode()); // 合法下架
        assertEquals(200, adminController.changeStatus(adminToken, p.getId(), Map.of("status", 1)).getCode()); // 合法上架

        Map<String, Object> nullDelta = new HashMap<>();
        nullDelta.put("delta", null);
        assertTrue(assertThrows(BizException.class, () -> adminController.adjustStock(adminToken, sku.getId(), nullDelta)).getMessage().contains("不能为空"));
        assertTrue(assertThrows(BizException.class, () -> adminController.adjustStock(adminToken, sku.getId(), Map.of("delta", 0))).getMessage().contains("不能为 0"));
        assertTrue(assertThrows(BizException.class, () -> adminController.adjustStock(adminToken, sku.getId(), Map.of("delta", -999))).getMessage().contains("库存不足"));
        var ok = adminController.adjustStock(adminToken, sku.getId(), Map.of("delta", 50));
        assertEquals(150, ((Map<?, ?>) ok.getData()).get("totalStock")); // 100+50，边界内正常
    }

    @Test
    void F11_管理端_积分调整与发货字段() {
        String adminToken = tokenStore.adminLogin("admin", "admin123");
        Member m = newMember();
        int before = m.getPoints();
        Map<String, Object> nullDelta = new HashMap<>();
        nullDelta.put("delta", null);
        assertTrue(assertThrows(BizException.class, () -> adminController.adjustPoints(adminToken, m.getId(), nullDelta)).getMessage().contains("不能为空"));
        assertTrue(assertThrows(BizException.class, () -> adminController.adjustPoints(adminToken, m.getId(), Map.of("delta", 0))).getMessage().contains("不能为 0"));
        assertEquals(before + 10, mallPoints(adminToken, m.getId(), 10)); // 合法 +10
        assertEquals(before, mallPoints(adminToken, m.getId(), -10)); // 负向扣减合法

        ProductSku sku = newSku("10", 100);
        MallOrder o = svc.createOrder(m.getId(), List.of(new MallService.OrderReqLine(sku.getId(), 1)), "张三", "13800001111", "地址");
        svc.pay(o.getOrderNo(), m.getId());
        assertTrue(assertThrows(BizException.class, () -> svc.ship(o.getOrderNo(), "")).getMessage().contains("物流单号不能为空"));
        assertTrue(assertThrows(BizException.class, () -> svc.ship(o.getOrderNo(), null)).getMessage().contains("物流单号不能为空"));
        svc.ship(o.getOrderNo(), "SF-FV-001"); // 合法发货
        assertEquals("SHIPPED", orderRepo.findByOrderNo(o.getOrderNo()).orElseThrow().getStatus());
    }

    @Test
    void F12_管理端_登录字段() {
        assertNull(tokenStore.adminLogin("admin", "wrong"), "错误密码应返回 null");
        assertNull(tokenStore.adminLogin("hacker", "admin123"), "错误用户名应返回 null");
        assertNotNull(tokenStore.adminLogin("admin", "admin123"), "正确账号应签发 token");
        R<Map<String, String>> resp = adminController.login(Map.of("username", "admin", "password", "000000"));
        assertEquals(500, resp.getCode(), "错误凭证应返回失败响应");
    }

    private int mallPoints(String adminToken, Long memberId, int delta) {
        Map<?, ?> r = (Map<?, ?>) adminController.adjustPoints(adminToken, memberId, Map.of("delta", delta)).getData();
        return (Integer) r.get("points");
    }
}
