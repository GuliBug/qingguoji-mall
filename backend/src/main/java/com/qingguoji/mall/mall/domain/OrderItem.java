package com.qingguoji.mall.mall.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 订单明细行（售后以明细行为粒度，A-01）
 *
 * @author qingguoji
 */
@Data
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Long skuId;

    /** 商品快照 */
    private String productName;

    private String spec;

    private BigDecimal price;

    private Integer qty;

    /** 该行实付（折扣后） */
    private BigDecimal payAmount;

    /** 已申请售后数量 */
    private Integer afterSaleQty;
}
