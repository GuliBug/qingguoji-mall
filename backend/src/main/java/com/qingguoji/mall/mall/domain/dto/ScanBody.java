package com.qingguoji.mall.mall.domain.dto;

import lombok.Data;

/**
 * 扫码积分请求体（V-04）
 *
 * @author qingguoji
 */
@Data
public class ScanBody {

    /** 防伪码 */
    private String code;
}
