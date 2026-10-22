package com.qingguoji.mall.mall.domain.dto;

import lombok.Data;

/**
 * 售后审核请求体（A-05）
 *
 * @author qingguoji
 */
@Data
public class AuditBody {

    /** true 通过 false 拒绝 */
    private Boolean pass;

    /** 拒绝原因 */
    private String rejectReason;
}
