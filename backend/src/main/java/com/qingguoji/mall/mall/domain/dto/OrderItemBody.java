package com.qingguoji.mall.mall.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单明细行请求体（O-01）
 *
 * @author qingguoji
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemBody {

    private Long skuId;

    private Integer qty;
}
