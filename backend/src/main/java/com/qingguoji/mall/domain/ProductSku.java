package com.qingguoji.mall.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/** SKU（P-05 规格 + S-01 库存结构：可售 = 总库存 - 锁定库存） */
@Data
@Entity
@Table(name = "product_sku")
public class ProductSku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    /** 规格：单包/整条5包/整箱50包 */
    private String spec;

    private BigDecimal price;

    /** 总库存 */
    private Integer totalStock;

    /** 锁定库存（下单预占） */
    private Integer lockedStock;

    /** 商品编码 */
    private String skuCode;

    @Transient
    public int availableStock() {
        return totalStock - lockedStock;
    }
}
