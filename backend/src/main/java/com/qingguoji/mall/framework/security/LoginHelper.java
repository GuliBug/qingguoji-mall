package com.qingguoji.mall.framework.security;

import com.qingguoji.mall.common.exception.ServiceException;

/**
 * 登录上下文工具（参考若依 SecurityUtils / LoginHelper）
 *
 * <p>由 {@link LoginInterceptor} 在请求进入时写入、请求结束时清理，业务层无需再解析 X-Token。
 *
 * @author qingguoji
 */
public class LoginHelper {

    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    private LoginHelper() {}

    public static void setLoginUser(LoginUser loginUser) {
        CONTEXT.set(loginUser);
    }

    public static LoginUser getLoginUser() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /** 当前买家会员 ID，未登录抛业务异常 */
    public static Long getMemberId() {
        LoginUser user = CONTEXT.get();
        if (user == null || user.getMemberId() == null) {
            throw new ServiceException("请先登录");
        }
        return user.getMemberId();
    }

    /** 当前管理员账号，未登录抛业务异常 */
    public static String getAdminName() {
        LoginUser user = CONTEXT.get();
        if (user == null || !user.isAdmin()) {
            throw new ServiceException("管理员未登录");
        }
        return user.getAdminName();
    }

    /** 当前会话是否管理端 */
    public static boolean isAdmin() {
        LoginUser user = CONTEXT.get();
        return user != null && user.isAdmin();
    }
}
