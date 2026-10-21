package com.qingguoji.mall.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后单（A-01~A-09）
 * type: REFUND_ONLY 仅退款 / RETURN_REFUND 退货退款
 * status: WAIT_AUDIT 待审核 → REFUNDING 退款中 / WAIT_RETURN 待买家寄回 → WAIT_RECEIVE 待商家收货 → DONE 已完成 / REJECTED 已拒绝 / CANCELED 已撤销
 */
@Data
@Entity
@Table(name = "after_sale")
public class AfterSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String afterNo;

    private Long orderId;

    private Long memberId;

    private Long orderItemId;

    /** REFUND_ONLY / RETURN_REFUND */
    private String type;

    private Integer qty;

    private BigDecimal refundAmount;

    /** 原因：七天无理由/包装破损/口感异常/发错货/其他 */
    private String reason;

    private String description;

    /** WAIT_AUDIT / REFUNDING / WAIT_RETURN / WAIT_RECEIVE / DONE / REJECTED / CANCELED */
    private String status;

    /** 拒绝原因 */
    private String rejectReason;

    /** 买家退货物流单号 */
    private String returnLogisticsNo;

    private LocalDateTime createdAt;
}
