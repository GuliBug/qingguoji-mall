package com.qingguoji.mall.mall.service.impl;

import com.qingguoji.mall.common.constant.Constants;
import com.qingguoji.mall.common.exception.ServiceException;
import com.qingguoji.mall.common.utils.StringUtils;
import com.qingguoji.mall.mall.domain.MallOrder;
import com.qingguoji.mall.mall.domain.Member;
import com.qingguoji.mall.mall.domain.OrderItem;
import com.qingguoji.mall.mall.domain.Product;
import com.qingguoji.mall.mall.domain.ProductSku;
import com.qingguoji.mall.mall.domain.dto.OrderBody;
import com.qingguoji.mall.mall.domain.dto.OrderItemBody;
import com.qingguoji.mall.mall.repository.MemberRepository;
import com.qingguoji.mall.mall.repository.OrderItemRepository;
import com.qingguoji.mall.mall.repository.OrderRepository;
import com.qingguoji.mall.mall.repository.ProductRepository;
import com.qingguoji.mall.mall.repository.ProductSkuRepository;
import com.qingguoji.mall.mall.service.IMemberService;
import com.qingguoji.mall.mall.service.IOrderService;
import com.qingguoji.mall.mall.service.IProductService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单服务实现
 *
 * <p>对应 spec 5.3 时序图：下单预占 → 支付扣减 → 取消释放。
 *
 * @author qingguoji
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    /** 支付超时分钟数（AC-02） */
    private static final int PAY_TIMEOUT_MINUTES = 15;

    /** 满额包邮门槛 */
    private static final BigDecimal FREE_FREIGHT_THRESHOLD = new BigDecimal("99");

    /** 未达门槛运费 */
    private static final BigDecimal FREIGHT = new BigDecimal("8");

    private static final int STATUS_ON_SALE = 1;

    private static final DateTimeFormatter ORDER_NO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final IProductService productService;
    private final IMemberService memberService;

    private final AtomicLong orderSeq = new AtomicLong(System.currentTimeMillis() % 1000000);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallOrder createOrder(Long memberId, OrderBody body) {
        List<OrderItemBody> lines = body == null ? null : body.getLines();
        if (lines == null || lines.isEmpty()) {
            throw new ServiceException("订单明细不能为空");
        }
        if (StringUtils.isEmpty(body.getReceiver())) {
            throw new ServiceException("收货人不能为空");
        }
        if (!StringUtils.isPhone(body.getReceiverPhone())) {
            throw new ServiceException("收货人手机号格式不正确");
        }
        if (StringUtils.isEmpty(body.getAddress())) {
            throw new ServiceException("收货地址不能为空");
        }
        Member member = memberService.selectMemberById(memberId);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal goodsAmount = BigDecimal.ZERO;

        // 1. 逐行预占库存（DB 条件更新，任一失败即回滚全部 —— S-02 原子性）
        for (OrderItemBody line : lines) {
            if (line == null || line.getSkuId() == null) {
                throw new ServiceException("订单明细 SKU 不能为空");
            }
            ProductSku sku =
                    productSkuRepository
                            .findById(line.getSkuId())
                            .orElseThrow(() -> new ServiceException("SKU 不存在: " + line.getSkuId()));
            Product product =
                    productRepository
                            .findById(sku.getProductId())
                            .orElseThrow(() -> new ServiceException("商品不存在"));
            if (product.getStatus() == null || product.getStatus() != STATUS_ON_SALE) {
                throw new ServiceException("商品「" + product.getName() + "」已下架");
            }
            if (line.getQty() == null || line.getQty() <= 0) {
                throw new ServiceException("购买数量必须大于 0");
            }
            if (!productService.lockStock(sku.getId(), line.getQty())) {
                throw new ServiceException(
                        "商品「" + product.getName() + " " + sku.getSpec() + "」库存不足");
            }

            BigDecimal linePay = sku.getPrice().multiply(BigDecimal.valueOf(line.getQty()));
            goodsAmount = goodsAmount.add(linePay);

            OrderItem item = new OrderItem();
            item.setSkuId(sku.getId());
            item.setProductName(product.getName());
            item.setSpec(sku.getSpec());
            item.setPrice(sku.getPrice());
            item.setQty(line.getQty());
            item.setPayAmount(linePay);
            item.setAfterSaleQty(0);
            items.add(item);
        }

        // 2. 会员折扣（V-02）与运费（C-04 满 99 包邮）
        BigDecimal discount =
                goodsAmount
                        .multiply(BigDecimal.valueOf(1 - member.discount()))
                        .setScale(2, RoundingMode.HALF_UP);
        BigDecimal payAmount = goodsAmount.subtract(discount);
        BigDecimal freight =
                payAmount.compareTo(FREE_FREIGHT_THRESHOLD) >= 0 ? BigDecimal.ZERO : FREIGHT;
        payAmount = payAmount.add(freight);

        // 3. 创建订单
        MallOrder order = new MallOrder();
        order.setOrderNo(
                Constants.ORDER_NO_PREFIX
                        + LocalDateTime.now().format(ORDER_NO_FORMATTER)
                        + String.format("%06d", orderSeq.incrementAndGet() % 1000000));
        order.setMemberId(memberId);
        order.setGoodsAmount(goodsAmount);
        order.setDiscountAmount(discount);
        order.setFreight(freight);
        order.setPayAmount(payAmount);
        order.setReceiver(body.getReceiver());
        order.setReceiverPhone(body.getReceiverPhone());
        order.setAddress(body.getAddress());
        order.setStatus("WAIT_PAY");
        order.setPayDeadline(LocalDateTime.now().plusMinutes(PAY_TIMEOUT_MINUTES));
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemRepository.save(item);
        }
        order.setItems(items);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallOrder payOrder(String orderNo, Long memberId) {
        MallOrder order = getOrderByNo(orderNo);
        if (!order.getMemberId().equals(memberId)) {
            throw new ServiceException("无权操作该订单");
        }
        if (!"WAIT_PAY".equals(order.getStatus())) {
            throw new ServiceException("订单状态不允许支付（幂等保护）");
        }

        Member member = memberService.selectMemberById(memberId);
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            productService.deductStock(item.getSkuId(), item.getQty());
        }

        order.setStatus("PAID");
        orderRepository.save(order);

        // V-03 积分 = 实付 × 等级倍率；成长值 = 实付金额
        int points = (int) Math.round(order.getPayAmount().doubleValue() * member.pointRate());
        memberService.addPoints(memberId, "EARN", points, "订单消费 " + order.getOrderNo());
        member.setGrowth(member.getGrowth() + order.getPayAmount().doubleValue());
        memberRepository.save(member);
        order.setPointsEarned(points);
        order.setGrowthEarned(order.getPayAmount().doubleValue());
        log.info("订单 {} 支付成功，发放积分 {}，成长值 {}", orderNo, points, order.getGrowthEarned());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, Long memberId) {
        MallOrder order = getOrderByNo(orderNo);
        if (memberId != null && !order.getMemberId().equals(memberId)) {
            throw new ServiceException("无权操作该订单");
        }
        // 幂等：非待付款订单直接返回
        if (!"WAIT_PAY".equals(order.getStatus())) {
            return;
        }
        releaseOrderStock(order);
        order.setStatus("CANCELED");
        orderRepository.save(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(String orderNo, Long memberId) {
        MallOrder order = getOrderByNo(orderNo);
        if (!order.getMemberId().equals(memberId)) {
            throw new ServiceException("无权操作该订单");
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new ServiceException("订单状态不允许确认收货");
        }
        order.setStatus("COMPLETED");
        orderRepository.save(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo, String logisticsNo) {
        if (StringUtils.isEmpty(logisticsNo)) {
            throw new ServiceException("物流单号不能为空");
        }
        MallOrder order = getOrderByNo(orderNo);
        if (!"PAID".equals(order.getStatus())) {
            throw new ServiceException("订单状态不允许发货");
        }
        order.setStatus("SHIPPED");
        order.setLogisticsNo(logisticsNo);
        orderRepository.save(order);
    }

    @Override
    public Page<MallOrder> selectMemberOrderPage(Long memberId, String status, Pageable pageable) {
        return StringUtils.isEmpty(status)
                ? orderRepository.findByMemberId(memberId, pageable)
                : orderRepository.findByMemberIdAndStatus(memberId, status, pageable);
    }

    @Override
    public Page<MallOrder> selectOrderPage(String status, Pageable pageable) {
        return StringUtils.isEmpty(status)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
    }

    @Override
    public List<OrderItem> selectItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelExpiredOrders() {
        List<MallOrder> expired =
                orderRepository.findByStatusAndPayDeadlineBefore("WAIT_PAY", LocalDateTime.now());
        for (MallOrder order : expired) {
            releaseOrderStock(order);
            order.setStatus("CANCELED");
            orderRepository.save(order);
        }
        return expired.size();
    }

    /** 释放订单占用的库存（S-04） */
    private void releaseOrderStock(MallOrder order) {
        for (OrderItem item : orderItemRepository.findByOrderId(order.getId())) {
            productService.releaseStock(item.getSkuId(), item.getQty());
        }
    }

    private MallOrder getOrderByNo(String orderNo) {
        return orderRepository
                .findByOrderNo(orderNo)
                .orElseThrow(() -> new ServiceException("订单不存在"));
    }
}
