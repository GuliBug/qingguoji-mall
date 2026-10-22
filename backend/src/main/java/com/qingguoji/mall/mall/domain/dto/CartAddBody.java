package com.qingguoji.mall.mall.domain.dto;

import lombok.Data;

/**
 * 加入购物车请求体（C-01）
 *
 * @author qingguoji
 */
@Data
public class CartAddBody {

    private Long skuId;

    private Integer qty;
}
