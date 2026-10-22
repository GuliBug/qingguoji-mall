package com.qingguoji.mall.mall.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import lombok.Data;

/**
 * SKU（P-05 规格 + S-01 库存结构：可售 = 总库存 - 锁定库存）
 *
 * @author qingguoji
 */
@Data
@Entity
@Table(name = "product_sku")
public class ProductSku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    /** 规格：单包 / 整条 5 包 / 整箱 50 包 */
    private String spec;

    private BigDecimal price;

    /** 总库存 */
    private Integer totalStock;

    /** 锁定库存（下单预占） */
    private Integer lockedStock;

    /** 商品编码 */
    private String skuCode;

    /** 可售库存 */
    @Transient
    public int availableStock() {
        int total = totalStock == null ? 0 : totalStock;
        int locked = lockedStock == null ? 0 : lockedStock;
        return total - locked;
    }
}
