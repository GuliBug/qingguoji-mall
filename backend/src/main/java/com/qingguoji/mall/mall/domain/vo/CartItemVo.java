package com.qingguoji.mall.mall.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 购物车展示对象（C-03：联表取商品名与价格）
 *
 * @author qingguoji
 */
@Data
public class CartItemVo {

    private Long cartId;

    private Long skuId;

    private String productName;

    private String spec;

    private String cover;

    private BigDecimal price;

    private Integer qty;

    /** 商品是否仍在售（下架后置灰） */
    private Boolean onSale;
}
