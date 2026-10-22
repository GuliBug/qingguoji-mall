package com.qingguoji.mall.mall.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 订单（O-01~O-08）
 *
 * <p>状态机：WAIT_PAY 待付款 → PAID 待发货 → SHIPPED 待收货 → COMPLETED 已完成 / CANCELED 已取消 / AFTER_SALE 售后中
 *
 * @author qingguoji
 */
@Data
@Entity
@Table(name = "mall_order")
public class MallOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单号：yyyyMMddHHmmss + 6 位序列 */
    @Column(unique = true)
    private String orderNo;

    private Long memberId;

    private BigDecimal goodsAmount;

    /** 会员折扣金额 */
    private BigDecimal discountAmount;

    private BigDecimal freight;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 收货人 / 电话 / 地址（简化为快照字段） */
    private String receiver;

    private String receiverPhone;

    private String address;

    /** WAIT_PAY / PAID / SHIPPED / COMPLETED / CANCELED / AFTER_SALE */
    private String status;

    /** 物流信息（发货后填） */
    private String logisticsNo;

    /** 支付成功后发放积分（V-03） */
    private Integer pointsEarned;

    /** 本单产生的成长值 */
    private Double growthEarned;

    private LocalDateTime payDeadline;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "orderId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items;
}
