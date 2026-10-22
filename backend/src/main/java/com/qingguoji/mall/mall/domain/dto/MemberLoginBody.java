package com.qingguoji.mall.mall.domain.dto;

import lombok.Data;

/**
 * 买家登录请求体（U-01）
 *
 * @author qingguoji
 */
@Data
public class MemberLoginBody {

    /** 手机号 */
    private String phone;

    /** 短信验证码（演示环境固定 123456） */
    private String smsCode;
}
