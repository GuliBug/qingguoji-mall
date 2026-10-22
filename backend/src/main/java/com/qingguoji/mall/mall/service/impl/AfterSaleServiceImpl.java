package com.qingguoji.mall.mall.service.impl;

import com.qingguoji.mall.common.constant.Constants;
import com.qingguoji.mall.common.exception.ServiceException;
import com.qingguoji.mall.common.utils.StringUtils;
import com.qingguoji.mall.mall.domain.AfterSale;
import com.qingguoji.mall.mall.domain.MallOrder;
import com.qingguoji.mall.mall.domain.Member;
import com.qingguoji.mall.mall.domain.OrderItem;
import com.qingguoji.mall.mall.domain.dto.AfterSaleBody;
import com.qingguoji.mall.mall.repository.AfterSaleRepository;
import com.qingguoji.mall.mall.repository.MemberRepository;
import com.qingguoji.mall.mall.repository.OrderItemRepository;
import com.qingguoji.mall.mall.repository.OrderRepository;
import com.qingguoji.mall.mall.service.IAfterSaleService;
import com.qingguoji.mall.mall.service.IMemberService;
import com.qingguoji.mall.mall.service.IProductService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 售后服务实现
 *
 * <p>仅退款：审核通过即完成；退货退款：审核 → 买家寄回 → 商家收货 → 退款并回补库存（S-07）。
 *
 * @author qingguoji
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AfterSaleServiceImpl implements IAfterSaleService {

    private static final String TYPE_REFUND_ONLY = "REFUND_ONLY";

    private static final String TYPE_RETURN_REFUND = "RETURN_REFUND";

    private static final DateTimeFormatter AFTER_NO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AfterSaleRepository afterSaleRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final IProductService productService;
    private final IMemberService memberService;

    private final AtomicLong afterSeq = new AtomicLong(System.currentTimeMillis() % 1000000);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AfterSale applyAfterSale(Long memberId, AfterSaleBody body) {
        if (body == null
                || (!TYPE_REFUND_ONLY.equals(body.getType())
                        && !TYPE_RETURN_REFUND.equals(body.getType()))) {
            throw new ServiceException("售后类型不合法（仅支持 REFUND_ONLY / RETURN_REFUND）");
        }
        if (StringUtils.isEmpty(body.getReason())) {
            throw new ServiceException("请填写售后原因");
        }
        OrderItem item =
                orderItemRepository
                        .findById(body.getOrderItemId())
                        .orElseThrow(() -> new ServiceException("订单明细不存在"));
        MallOrder order =
                orderRepository
                        .findById(item.getOrderId())
                        .orElseThrow(() -> new ServiceException("订单不存在"));
        if (!order.getMemberId().equals(memberId)) {
            throw new ServiceException("无权操作");
        }

        String orderStatus = order.getStatus();
        if (!"PAID".equals(orderStatus)
                && !"SHIPPED".equals(orderStatus)
                && !"COMPLETED".equals(orderStatus)) {
            throw new ServiceException("当前订单状态不可申请售后");
        }
        // A-02：已发货 / 已完成订单仅支持退货退款
        if (TYPE_REFUND_ONLY.equals(body.getType())
                && ("SHIPPED".equals(orderStatus) || "COMPLETED".equals(orderStatus))) {
            throw new ServiceException("已发货/已完成订单仅支持退货退款");
        }

        int qty = body.getQty() == null ? 0 : body.getQty();
        int saleableQty = item.getQty() - item.getAfterSaleQty();
        if (qty <= 0 || qty > saleableQty) {
            throw new ServiceException("售后数量不合法，最多可退 " + saleableQty + " 件");
        }

        // A-09 金额约束：按明细实付比例
        BigDecimal unitPay =
                item.getPayAmount()
                        .divide(BigDecimal.valueOf(item.getQty()), 2, RoundingMode.HALF_UP);
        BigDecimal refund = unitPay.multiply(BigDecimal.valueOf(qty));

        AfterSale afterSale = new AfterSale();
        afterSale.setAfterNo(
                Constants.AFTER_SALE_NO_PREFIX
                        + LocalDateTime.now().format(AFTER_NO_FORMATTER)
                        + String.format("%04d", afterSeq.incrementAndGet() % 10000));
        afterSale.setOrderId(order.getId());
        afterSale.setMemberId(memberId);
        afterSale.setOrderItemId(item.getId());
        afterSale.setType(body.getType());
        afterSale.setQty(qty);
        afterSale.setRefundAmount(refund);
        afterSale.setReason(body.getReason());
        afterSale.setDescription(body.getDescription());
        afterSale.setStatus("WAIT_AUDIT");
        afterSale.setCreatedAt(LocalDateTime.now());
        afterSaleRepository.save(afterSale);

        item.setAfterSaleQty(item.getAfterSaleQty() + qty);
        orderItemRepository.save(item);
        if (order.getItems() == null || item.getAfterSaleQty().equals(item.getQty())) {
            order.setStatus("AFTER_SALE");
            orderRepository.save(order);
        }
        return afterSale;
    }

    @Override
    public Page<AfterSale> selectMyAfterSalePage(Long memberId, Pageable pageable) {
        return afterSaleRepository.findByMemberId(memberId, pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fillReturnLogistics(Long afterSaleId, Long memberId, String logisticsNo) {
        if (StringUtils.isEmpty(logisticsNo)) {
            throw new ServiceException("退货物流单号不能为空");
        }
        AfterSale afterSale = getAfterSale(afterSaleId);
        if (!afterSale.getMemberId().equals(memberId)
                || !"WAIT_RETURN".equals(afterSale.getStatus())) {
            throw new ServiceException("当前状态不可填写退货物流");
        }
        afterSale.setReturnLogisticsNo(logisticsNo);
        afterSale.setStatus("WAIT_RECEIVE");
        afterSaleRepository.save(afterSale);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AfterSale audit(Long afterSaleId, boolean pass, String rejectReason) {
        AfterSale afterSale = getAfterSale(afterSaleId);
        if (!"WAIT_AUDIT".equals(afterSale.getStatus())) {
            throw new ServiceException("售后单状态不允许审核");
        }
        if (!pass) {
            afterSale.setStatus("REJECTED");
            afterSale.setRejectReason(StringUtils.isEmpty(rejectReason) ? "不符合售后条件" : rejectReason);
            afterSaleRepository.save(afterSale);
            restoreOrderIfNeeded(afterSale);
            return afterSale;
        }
        if (TYPE_REFUND_ONLY.equals(afterSale.getType())) {
            // A-06 仅退款：直接完成（模拟退款），无库存回补
            finishRefund(afterSale);
        } else {
            // A-07 退货退款：进入待买家寄回
            afterSale.setStatus("WAIT_RETURN");
            afterSaleRepository.save(afterSale);
        }
        return afterSale;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AfterSale confirmReturn(Long afterSaleId) {
        AfterSale afterSale = getAfterSale(afterSaleId);
        if (!"WAIT_RECEIVE".equals(afterSale.getStatus())) {
            throw new ServiceException("售后单状态不允许确认收货");
        }
        finishRefund(afterSale);
        return afterSale;
    }

    @Override
    public Page<AfterSale> selectAfterSalePage(String status, Pageable pageable) {
        return StringUtils.isEmpty(status)
                ? afterSaleRepository.findAll(pageable)
                : afterSaleRepository.findByStatus(status, pageable);
    }

    /** 退款完成：模拟退款到账 + 退货回补库存 + 按比例扣回积分与成长值 */
    private void finishRefund(AfterSale afterSale) {
        OrderItem item =
                orderItemRepository
                        .findById(afterSale.getOrderItemId())
                        .orElseThrow(() -> new ServiceException("订单明细不存在"));

        // S-07 退货退款回补库存
        if (TYPE_RETURN_REFUND.equals(afterSale.getType())) {
            productService.restock(item.getSkuId(), afterSale.getQty());
        }

        // AC-12 按退款金额比例扣回积分与成长值
        Member member = memberService.selectMemberById(afterSale.getMemberId());
        MallOrder order =
                orderRepository
                        .findById(item.getOrderId())
                        .orElseThrow(() -> new ServiceException("订单不存在"));
        double orderPay = order.getPayAmount().doubleValue();
        double ratio = orderPay == 0 ? 0 : afterSale.getRefundAmount().doubleValue() / orderPay;
        int pointsEarned = order.getPointsEarned() == null ? 0 : order.getPointsEarned();
        int backPoints = (int) Math.round(pointsEarned * ratio);
        if (backPoints > 0) {
            memberService.addPoints(
                    afterSale.getMemberId(),
                    "REFUND_BACK",
                    -backPoints,
                    "退款扣回积分 " + afterSale.getAfterNo());
        }
        double growthEarned = order.getGrowthEarned() == null ? 0 : order.getGrowthEarned();
        member.setGrowth(Math.max(0, member.getGrowth() - growthEarned * ratio));
        memberRepository.save(member);

        afterSale.setStatus("DONE");
        afterSaleRepository.save(afterSale);
        restoreOrderIfNeeded(afterSale);
        log.info(
                "售后单 {} 退款完成 ¥{}，扣回积分 {}",
                afterSale.getAfterNo(),
                afterSale.getRefundAmount(),
                backPoints);
    }

    /** 拒绝 / 完成售后后，若仍有明细未退完则订单恢复原状态 */
    private void restoreOrderIfNeeded(AfterSale afterSale) {
        MallOrder order =
                orderRepository
                        .findById(afterSale.getOrderId())
                        .orElseThrow(() -> new ServiceException("订单不存在"));
        if (!"AFTER_SALE".equals(order.getStatus())) {
            return;
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        int totalAfter = items.stream().mapToInt(OrderItem::getAfterSaleQty).sum();
        int totalQty = items.stream().mapToInt(OrderItem::getQty).sum();
        if (totalAfter < totalQty) {
            order.setStatus(order.getLogisticsNo() == null ? "PAID" : "SHIPPED");
            orderRepository.save(order);
        }
    }

    private AfterSale getAfterSale(Long afterSaleId) {
        return afterSaleRepository
                .findById(afterSaleId)
                .orElseThrow(() -> new ServiceException("售后单不存在"));
    }
}
