package com.qingguoji.mall.mall.domain.dto;

import lombok.Data;

/**
 * 修改购物车数量请求体（C-02）
 *
 * @author qingguoji
 */
@Data
public class CartUpdateBody {

    private Integer qty;
}
