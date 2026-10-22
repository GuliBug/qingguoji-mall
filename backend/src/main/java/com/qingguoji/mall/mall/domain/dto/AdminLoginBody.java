package com.qingguoji.mall.mall.domain.dto;

import lombok.Data;

/**
 * 管理员登录请求体（M-06）
 *
 * @author qingguoji
 */
@Data
public class AdminLoginBody {

    private String username;

    private String password;
}
