package com.qingguoji.mall.mall.domain.dto;

import lombok.Data;

/**
 * 物流单号请求体（O-08 发货 / A-07 退货寄回）
 *
 * @author qingguoji
 */
@Data
public class LogisticsBody {

    private String logisticsNo;
}
