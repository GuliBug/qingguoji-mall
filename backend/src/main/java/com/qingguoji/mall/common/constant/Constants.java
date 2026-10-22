package com.qingguoji.mall.common.constant;

/**
 * 通用常量（参考若依 Constants）
 *
 * @author qingguoji
 */
public class Constants {

    /** 令牌请求头（前后端约定，若依原版为 Authorization） */
    public static final String TOKEN_HEADER = "X-Token";

    /** 卖家管理员默认账号（M-06 演示环境） */
    public static final String ADMIN_USERNAME = "admin";

    /** 卖家管理员默认密码 */
    public static final String ADMIN_PASSWORD = "admin123";

    /** 演示环境固定短信验证码 */
    public static final String DEMO_SMS_CODE = "123456";

    /** 卖家管理端令牌前缀 */
    public static final String ADMIN_TOKEN_PREFIX = "ADM-";

    /** 买家会员令牌前缀 */
    public static final String MEMBER_TOKEN_PREFIX = "MBR-";

    /** 订单号前缀 */
    public static final String ORDER_NO_PREFIX = "QGJ";

    /** 售后单号前缀 */
    public static final String AFTER_SALE_NO_PREFIX = "AH";

    private Constants() {}
}
