package com.qingguoji.mall.mall.domain.dto;

import lombok.Data;

/**
 * 商品上下架请求体（M-02）
 *
 * @author qingguoji
 */
@Data
public class StatusBody {

    /** 1 上架 0 下架 */
    private Integer status;
}
