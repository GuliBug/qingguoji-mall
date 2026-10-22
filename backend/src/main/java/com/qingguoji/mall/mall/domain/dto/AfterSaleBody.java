package com.qingguoji.mall.mall.domain.dto;

import lombok.Data;

/**
 * 申请售后请求体（A-02 / A-03）
 *
 * @author qingguoji
 */
@Data
public class AfterSaleBody {

    private Long orderItemId;

    /** REFUND_ONLY / RETURN_REFUND */
    private String type;

    private Integer qty;

    private String reason;

    private String description;
}
