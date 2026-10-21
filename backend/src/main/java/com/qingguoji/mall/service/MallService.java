package com.qingguoji.mall.service;

import com.qingguoji.mall.common.BizException;
import com.qingguoji.mall.domain.*;
import com.qingguoji.mall.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 商城核心服务：下单-库存预占（S-02）、支付扣减（S-03）、取消释放（S-04）、
 * 会员积分（V-03）、售后退单（A-01~A-09）。对应 spec.md 5.2/5.3/5.4 时序图。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallService {

    private final MemberRepo memberRepo;
    private final ProductRepo productRepo;
    private final ProductSkuRepo skuRepo;
    private final CartRepo cartRepo;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final AfterSaleRepo afterSaleRepo;
    private final PointsLogRepo pointsLogRepo;
    private final ScanCodeRepo scanCodeRepo;

    private static final int PAY_TIMEOUT_MINUTES = 15;
    private static final int SCAN_DAILY_LIMIT = 20;
    private static final int SCAN_POINTS = 10;
    private final AtomicLong orderSeq = new AtomicLong(System.currentTimeMillis() % 1000000);
    private final AtomicLong afterSeq = new AtomicLong(System.currentTimeMillis() % 1000000);

    // ==================== 会员 ====================

    /** U-01 手机号验证码登录（验证码模拟 123456），新用户自动注册 */
    @Transactional
    public Member login(String phone, String smsCode) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BizException("手机号格式不正确（需 11 位，1 开头）");
        }
        if (smsCode == null || smsCode.isBlank()) {
            throw new BizException("验证码不能为空");
        }
        if (!"123456".equals(smsCode)) {
            throw new BizException("验证码错误（演示环境固定为 123456）");
        }
        Member m = memberRepo.findByPhone(phone).orElseGet(() -> {
            Member n = new Member();
            n.setPhone(phone);
            n.setNickname("会员" + phone.substring(phone.length() - 4));
            n.setGrowth(0.0);
            n.setPoints(0);
            n.setCreatedAt(LocalDateTime.now());
            return memberRepo.save(n);
        });
        return m;
    }

    public Page<PointsLog> pointsLogs(Long memberId, int page, int size) {
        return pointsLogRepo.findByMemberId(memberId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    /** V-05 每日签到 +5 积分（每人每日一次，简化用当天是否有 CHECKIN 流水判断） */
    @Transactional
    public int checkin(Long memberId) {
        LocalDate today = LocalDate.now();
        boolean checked = pointsLogRepo.findByMemberId(memberId, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .anyMatch(l -> "CHECKIN".equals(l.getType()) && l.getCreatedAt().toLocalDate().equals(today));
        if (checked) throw new BizException("今日已签到");
        return addPoints(memberId, "CHECKIN", 5, "每日签到");
    }

    /** V-04 扫码积分：防伪码唯一 + 每日上限 20 包（AC-10） */
    @Transactional
    public int scanCode(Long memberId, String code) {
        if (code == null || code.isBlank()) throw new BizException("防伪码不能为空");
        ScanCode sc = scanCodeRepo.findByCode(code)
                .orElseThrow(() -> new BizException("防伪码不存在，请核对"));
        if (Boolean.TRUE.equals(sc.getUsed())) throw new BizException("该码已被使用");

        LocalDate today = LocalDate.now();
        long todayScans = pointsLogRepo.findByMemberId(memberId, PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .filter(l -> "SCAN".equals(l.getType()) && l.getCreatedAt().toLocalDate().equals(today))
                .count();
        if (todayScans >= SCAN_DAILY_LIMIT) throw new BizException("今日扫码已达上限（20 包）");

        sc.setUsed(true);
        sc.setUsedBy(memberId);
        sc.setUsedAt(LocalDateTime.now());
        scanCodeRepo.save(sc);
        return addPoints(memberId, "SCAN", SCAN_POINTS, "扫码积分 " + code);
    }

    @Transactional
    public int addPoints(Long memberId, String type, int amount, String remark) {
        Member m = memberRepo.findById(memberId).orElseThrow(() -> new BizException("会员不存在"));
        m.setPoints(m.getPoints() + amount);
        memberRepo.save(m);
        PointsLog log = new PointsLog();
        log.setMemberId(memberId);
        log.setType(type);
        log.setAmount(amount);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        pointsLogRepo.save(log);
        return m.getPoints();
    }

    // ==================== 商品 / 购物车 ====================

    public Page<Product> products(String category, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sales"));
        return category == null || category.isBlank()
                ? productRepo.findByStatus(1, pr)
                : productRepo.findByCategoryAndStatus(category, 1, pr);
    }

    public List<ProductSku> skusOf(Long productId) {
        return skuRepo.findByProductId(productId);
    }

    public List<CartView> cartView(Long memberId) {
        List<CartView> views = new ArrayList<>();
        for (CartItem ci : cartRepo.findByMemberId(memberId)) {
            ProductSku sku = skuRepo.findById(ci.getSkuId()).orElse(null);
            if (sku == null) continue;
            Product p = productRepo.findById(sku.getProductId()).orElse(null);
            CartView v = new CartView();
            v.setCartId(ci.getId());
            v.setSkuId(sku.getId());
            v.setQty(ci.getQty());
            v.setSpec(sku.getSpec());
            v.setPrice(sku.getPrice());
            v.setProductName(p == null ? "未知商品" : p.getName());
            v.setCover(p == null ? "#ccc" : p.getCover());
            v.setOnSale(p != null && p.getStatus() == 1);
            views.add(v);
        }
        return views;
    }

    /** C-01 加购：同一 SKU 合并数量；下架/删改商品不可加购（AC-07） */
    @Transactional
    public void addToCart(Long memberId, Long skuId, Integer qty) {
        if (skuId == null) throw new BizException("SKU 不能为空");
        if (qty == null || qty <= 0) throw new BizException("购买数量必须大于 0");
        if (qty > 999) throw new BizException("单次加购数量不能超过 999");
        ProductSku sku = skuRepo.findById(skuId).orElseThrow(() -> new BizException("SKU 不存在"));
        Product p = productRepo.findById(sku.getProductId()).orElseThrow(() -> new BizException("商品不存在"));
        if (p.getStatus() != 1) throw new BizException("商品已下架，无法加入购物车");
        CartItem ci = cartRepo.findByMemberIdAndSkuId(memberId, skuId).orElseGet(() -> {
            CartItem n = new CartItem();
            n.setMemberId(memberId);
            n.setSkuId(skuId);
            n.setQty(0);
            return n;
        });
        ci.setQty(ci.getQty() + qty);
        cartRepo.save(ci);
    }

    @Transactional
    public void updateCart(Long memberId, Long cartId, Integer qty) {
        if (qty == null) throw new BizException("数量不能为空");
        CartItem ci = cartRepo.findById(cartId).orElseThrow(() -> new BizException("购物车项不存在"));
        if (!ci.getMemberId().equals(memberId)) throw new BizException("无权操作");
        if (qty <= 0) cartRepo.delete(ci);
        else { ci.setQty(qty); cartRepo.save(ci); }
    }

    // ==================== 订单 ====================

    /** O-01/O-02 下单：库存预占 + 订单创建（同事务，AC-01 防超卖） */
    @Transactional
    public MallOrder createOrder(Long memberId, List<OrderReqLine> lines, String receiver, String phone, String address) {
        if (lines == null || lines.isEmpty()) throw new BizException("订单明细不能为空");
        if (receiver == null || receiver.isBlank()) throw new BizException("收货人不能为空");
        if (phone == null || !phone.matches("^1\\d{10}$")) throw new BizException("收货人手机号格式不正确");
        if (address == null || address.isBlank()) throw new BizException("收货地址不能为空");
        Member m = memberRepo.findById(memberId).orElseThrow(() -> new BizException("会员不存在"));

        List<OrderItem> items = new ArrayList<>();
        BigDecimal goodsAmount = BigDecimal.ZERO;

        // 1. 逐行预占库存（DB 条件更新，任一失败即回滚全部 —— S-02 原子性）
        for (OrderReqLine line : lines) {
            if (line == null || line.skuId() == null) throw new BizException("订单明细 SKU 不能为空");
            ProductSku sku = skuRepo.findById(line.skuId()).orElseThrow(() -> new BizException("SKU 不存在: " + line.skuId()));
            Product p = productRepo.findById(sku.getProductId()).orElseThrow(() -> new BizException("商品不存在"));
            if (p.getStatus() != 1) throw new BizException("商品「" + p.getName() + "」已下架");
            if (line.qty <= 0) throw new BizException("购买数量必须大于 0");

            int locked = skuRepo.lockStock(sku.getId(), line.qty);
            if (locked == 0) throw new BizException("商品「" + p.getName() + " " + sku.getSpec() + "」库存不足");

            BigDecimal linePay = sku.getPrice().multiply(BigDecimal.valueOf(line.qty));
            goodsAmount = goodsAmount.add(linePay);

            OrderItem item = new OrderItem();
            item.setSkuId(sku.getId());
            item.setProductName(p.getName());
            item.setSpec(sku.getSpec());
            item.setPrice(sku.getPrice());
            item.setQty(line.qty);
            item.setPayAmount(linePay);
            item.setAfterSaleQty(0);
            items.add(item);
        }

        // 2. 会员折扣（V-02）与运费（C-04 满99包邮）
        BigDecimal discount = goodsAmount.multiply(BigDecimal.valueOf(1 - m.discount()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal payAmount = goodsAmount.subtract(discount);
        BigDecimal freight = payAmount.compareTo(new BigDecimal("99")) >= 0
                ? BigDecimal.ZERO : new BigDecimal("8");
        payAmount = payAmount.add(freight);

        // 3. 创建订单
        MallOrder order = new MallOrder();
        order.setOrderNo("QGJ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", orderSeq.incrementAndGet() % 1000000));
        order.setMemberId(memberId);
        order.setGoodsAmount(goodsAmount);
        order.setDiscountAmount(discount);
        order.setFreight(freight);
        order.setPayAmount(payAmount);
        order.setReceiver(receiver);
        order.setReceiverPhone(phone);
        order.setAddress(address);
        order.setStatus("WAIT_PAY");
        order.setPayDeadline(LocalDateTime.now().plusMinutes(PAY_TIMEOUT_MINUTES));
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepo.save(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemRepo.save(item);
        }
        order.setItems(items);
        return order;
    }

    /** O-03 模拟支付：订单→待发货，锁定→实际扣减，发放积分+成长值（S-03/V-03） */
    @Transactional
    public MallOrder pay(String orderNo, Long memberId) {
        MallOrder order = orderRepo.findByOrderNo(orderNo).orElseThrow(() -> new BizException("订单不存在"));
        if (!order.getMemberId().equals(memberId)) throw new BizException("无权操作该订单");
        if (!"WAIT_PAY".equals(order.getStatus())) throw new BizException("订单状态不允许支付（幂等保护）");

        Member m = memberRepo.findById(memberId).orElseThrow(() -> new BizException("会员不存在"));

        List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
        for (OrderItem item : items) {
            if (skuRepo.deductStock(item.getSkuId(), item.getQty()) == 0) {
                throw new BizException("库存异常，请联系客服");
            }
        }

        order.setStatus("PAID");
        orderRepo.save(order);

        // V-03 积分 = 实付 × 等级倍率；成长值 = 实付金额
        int points = (int) Math.round(order.getPayAmount().doubleValue() * m.pointRate());
        addPoints(memberId, "EARN", points, "订单消费 " + order.getOrderNo());
        m.setGrowth(m.getGrowth() + order.getPayAmount().doubleValue());
        memberRepo.save(m);
        order.setPointsEarned(points);
        order.setGrowthEarned(order.getPayAmount().doubleValue());
        log.info("订单 {} 支付成功，发放积分 {}，成长值 {}", orderNo, points, order.getGrowthEarned());
        return order;
    }

    /** O-05 取消订单：释放预占库存（幂等：状态校验） */
    @Transactional
    public void cancel(String orderNo, Long memberId) {
        MallOrder order = orderRepo.findByOrderNo(orderNo).orElseThrow(() -> new BizException("订单不存在"));
        if (memberId != null && !order.getMemberId().equals(memberId)) throw new BizException("无权操作该订单");
        if (!"WAIT_PAY".equals(order.getStatus())) return; // 幂等返回
        releaseOrderStock(order);
        order.setStatus("CANCELED");
        orderRepo.save(order);
    }

    /** O-07 确认收货 */
    @Transactional
    public void confirmReceive(String orderNo, Long memberId) {
        MallOrder order = orderRepo.findByOrderNo(orderNo).orElseThrow(() -> new BizException("订单不存在"));
        if (!order.getMemberId().equals(memberId)) throw new BizException("无权操作该订单");
        if (!"SHIPPED".equals(order.getStatus())) throw new BizException("订单状态不允许确认收货");
        order.setStatus("COMPLETED");
        orderRepo.save(order);
    }

    /** O-08 管理端发货 */
    @Transactional
    public void ship(String orderNo, String logisticsNo) {
        if (logisticsNo == null || logisticsNo.isBlank()) throw new BizException("物流单号不能为空");
        MallOrder order = orderRepo.findByOrderNo(orderNo).orElseThrow(() -> new BizException("订单不存在"));
        if (!"PAID".equals(order.getStatus())) throw new BizException("订单状态不允许发货");
        order.setStatus("SHIPPED");
        order.setLogisticsNo(logisticsNo);
        orderRepo.save(order);
    }

    private void releaseOrderStock(MallOrder order) {
        for (OrderItem item : orderItemRepo.findByOrderId(order.getId())) {
            skuRepo.releaseStock(item.getSkuId(), item.getQty());
        }
    }

    /** O-05 超时自动取消（定时任务调用，AC-02） */
    @Transactional
    public int cancelExpiredOrders() {
        List<MallOrder> expired = orderRepo.findByStatusAndPayDeadlineBefore("WAIT_PAY", LocalDateTime.now());
        for (MallOrder order : expired) {
            releaseOrderStock(order);
            order.setStatus("CANCELED");
            orderRepo.save(order);
        }
        return expired.size();
    }

    // ==================== 售后（A-01~A-09） ====================

    /** A-02/A-03 申请售后：校验类型、数量、金额（AC-05/AC-12） */
    @Transactional
    public AfterSale applyAfterSale(Long memberId, Long orderItemId, String type, int qty, String reason, String description) {
        if (!"REFUND_ONLY".equals(type) && !"RETURN_REFUND".equals(type)) {
            throw new BizException("售后类型不合法（仅支持 REFUND_ONLY / RETURN_REFUND）");
        }
        if (reason == null || reason.isBlank()) throw new BizException("请填写售后原因");
        OrderItem item = orderItemRepo.findById(orderItemId).orElseThrow(() -> new BizException("订单明细不存在"));
        MallOrder order = orderRepo.findById(item.getOrderId()).orElseThrow(() -> new BizException("订单不存在"));
        if (!order.getMemberId().equals(memberId)) throw new BizException("无权操作");

        String os = order.getStatus();
        if (!"PAID".equals(os) && !"SHIPPED".equals(os) && !"COMPLETED".equals(os)) {
            throw new BizException("当前订单状态不可申请售后");
        }
        if ("REFUND_ONLY".equals(type) && "WAIT_PAY".equals(os)) {
            throw new BizException("待付款订单无需售后，可直接取消");
        }
        // A-02：已收货后只能退货退款
        if ("REFUND_ONLY".equals(type) && ("SHIPPED".equals(os) || "COMPLETED".equals(os))) {
            throw new BizException("已发货/已完成订单仅支持退货退款");
        }

        int saleableQty = item.getQty() - item.getAfterSaleQty();
        if (qty <= 0 || qty > saleableQty) throw new BizException("售后数量不合法，最多可退 " + saleableQty + " 件");

        // A-09 金额约束：按明细实付比例
        BigDecimal unitPay = item.getPayAmount().divide(BigDecimal.valueOf(item.getQty()), 2, RoundingMode.HALF_UP);
        BigDecimal refund = unitPay.multiply(BigDecimal.valueOf(qty));

        AfterSale as = new AfterSale();
        as.setAfterNo("AH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", afterSeq.incrementAndGet() % 10000));
        as.setOrderId(order.getId());
        as.setMemberId(memberId);
        as.setOrderItemId(orderItemId);
        as.setType(type);
        as.setQty(qty);
        as.setRefundAmount(refund);
        as.setReason(reason);
        as.setDescription(description);
        as.setStatus("WAIT_AUDIT");
        as.setCreatedAt(LocalDateTime.now());
        afterSaleRepo.save(as);

        item.setAfterSaleQty(item.getAfterSaleQty() + qty);
        orderItemRepo.save(item);
        if (order.getItems() == null || item.getAfterSaleQty() == item.getQty()) {
            order.setStatus("AFTER_SALE");
            orderRepo.save(order);
        }
        return as;
    }

    /** A-05/A-06/A-07 商家审核：仅退款→完成；退货退款→待寄回。退款后扣回积分/成长值（AC-12） */
    @Transactional
    public AfterSale audit(Long afterSaleId, boolean pass, String rejectReason) {
        AfterSale as = afterSaleRepo.findById(afterSaleId).orElseThrow(() -> new BizException("售后单不存在"));
        if (!"WAIT_AUDIT".equals(as.getStatus())) throw new BizException("售后单状态不允许审核");

        if (!pass) {
            as.setStatus("REJECTED");
            as.setRejectReason(rejectReason == null ? "不符合售后条件" : rejectReason);
            afterSaleRepo.save(as);
            restoreOrderIfNeeded(as);
            return as;
        }

        if ("REFUND_ONLY".equals(as.getType())) {
            // A-06 仅退款：直接完成（模拟退款），无库存回补
            finishRefund(as);
        } else {
            // A-07 退货退款：进入待买家寄回
            as.setStatus("WAIT_RETURN");
            afterSaleRepo.save(as);
        }
        return as;
    }

    /** 买家寄回后商家确认收货 → 退款 + 库存回补（A-07/S-07） */
    @Transactional
    public AfterSale confirmReturn(Long afterSaleId) {
        AfterSale as = afterSaleRepo.findById(afterSaleId).orElseThrow(() -> new BizException("售后单不存在"));
        if (!"WAIT_RECEIVE".equals(as.getStatus())) throw new BizException("售后单状态不允许确认收货");
        finishRefund(as);
        return as;
    }

    @Transactional
    public void fillReturnLogistics(Long afterSaleId, Long memberId, String logisticsNo) {
        if (logisticsNo == null || logisticsNo.isBlank()) throw new BizException("退货物流单号不能为空");
        AfterSale as = afterSaleRepo.findById(afterSaleId).orElseThrow(() -> new BizException("售后单不存在"));
        if (!as.getMemberId().equals(memberId) || !"WAIT_RETURN".equals(as.getStatus())) {
            throw new BizException("当前状态不可填写退货物流");
        }
        as.setReturnLogisticsNo(logisticsNo);
        as.setStatus("WAIT_RECEIVE");
        afterSaleRepo.save(as);
    }

    /** 退款完成：模拟退款到账 + 退货回补库存 + 按比例扣回积分与成长值 */
    private void finishRefund(AfterSale as) {
        // 退货退款：库存回补（S-07）
        if ("RETURN_REFUND".equals(as.getType())) {
            OrderItem item = orderItemRepo.findById(as.getOrderItemId()).orElseThrow();
            skuRepo.restock(item.getSkuId(), as.getQty());
        }
        // AC-12 按退款金额比例扣回积分与成长值
        Member m = memberRepo.findById(as.getMemberId()).orElseThrow();
        OrderItem item = orderItemRepo.findById(as.getOrderItemId()).orElseThrow();
        MallOrder order = orderRepo.findById(item.getOrderId()).orElseThrow();
        double ratio = order.getPayAmount().doubleValue() == 0 ? 0
                : as.getRefundAmount().doubleValue() / order.getPayAmount().doubleValue();
        int backPoints = (int) Math.round((order.getPointsEarned() == null ? 0 : order.getPointsEarned()) * ratio);
        if (backPoints > 0) addPoints(as.getMemberId(), "REFUND_BACK", -backPoints, "退款扣回积分 " + as.getAfterNo());
        m.setGrowth(Math.max(0, m.getGrowth() - order.getGrowthEarned() * ratio));
        memberRepo.save(m);

        as.setStatus("DONE");
        afterSaleRepo.save(as);
        restoreOrderIfNeeded(as);
        log.info("售后单 {} 退款完成 ¥{}，扣回积分 {}", as.getAfterNo(), as.getRefundAmount(), backPoints);
    }

    /** 拒绝/完成售后后，若所有明细都不再售后中，订单恢复原状态 */
    private void restoreOrderIfNeeded(AfterSale as) {
        MallOrder order = orderRepo.findById(as.getOrderId()).orElseThrow();
        if (!"AFTER_SALE".equals(order.getStatus())) return;
        List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
        int totalAfter = items.stream().mapToInt(OrderItem::getAfterSaleQty).sum();
        int totalQty = items.stream().mapToInt(OrderItem::getQty).sum();
        if (totalAfter < totalQty) {
            order.setStatus(order.getLogisticsNo() == null ? "PAID" : "SHIPPED");
            orderRepo.save(order);
        }
    }

    // ==================== DTO ====================

    public record OrderReqLine(Long skuId, int qty) {}

    public static class OrderReq {
        public List<OrderReqLine> lines;
        public String receiver;
        public String receiverPhone;
        public String address;
    }

    @lombok.Data
    public static class CartView {
        private Long cartId;
        private Long skuId;
        private String productName;
        private String spec;
        private String cover;
        private java.math.BigDecimal price;
        private Integer qty;
        private Boolean onSale;
    }
}
