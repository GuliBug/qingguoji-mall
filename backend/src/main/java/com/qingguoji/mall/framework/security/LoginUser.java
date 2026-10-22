package com.qingguoji.mall.framework.security;

import lombok.Getter;

/**
 * 当前登录用户（参考若依 LoginUser）
 *
 * <p>买家与卖家管理端共用同一上下文：memberId 仅买家有值，adminName 仅管理端有值。
 *
 * @author qingguoji
 */
@Getter
public class LoginUser {

    /** 买家会员 ID（管理端为 null） */
    private final Long memberId;

    /** 管理员账号（买家为 null） */
    private final String adminName;

    /** 是否管理端会话 */
    private final boolean admin;

    private LoginUser(Long memberId, String adminName, boolean admin) {
        this.memberId = memberId;
        this.adminName = adminName;
        this.admin = admin;
    }

    public static LoginUser member(Long memberId) {
        return new LoginUser(memberId, null, false);
    }

    public static LoginUser admin(String adminName) {
        return new LoginUser(null, adminName, true);
    }
}
