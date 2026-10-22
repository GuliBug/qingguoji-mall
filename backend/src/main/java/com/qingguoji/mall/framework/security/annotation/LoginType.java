package com.qingguoji.mall.framework.security.annotation;

/**
 * 会话类型（参考若依的登录主体划分）
 *
 * @author qingguoji
 */
public enum LoginType {

    /** 买家会员（H5 端） */
    MEMBER,

    /** 卖家管理员（Web 管理端） */
    ADMIN
}
