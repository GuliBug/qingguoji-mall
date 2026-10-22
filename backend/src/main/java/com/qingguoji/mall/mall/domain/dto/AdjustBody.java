package com.qingguoji.mall.mall.domain.dto;

import lombok.Data;

/**
 * 数值调整请求体（管理端库存调整 / 积分调整共用）
 *
 * @author qingguoji
 */
@Data
public class AdjustBody {

    /** 调整数量，正数增加、负数扣减，不可为 0 */
    private Integer delta;

    /** 调整原因（留痕） */
    private String reason;
}
