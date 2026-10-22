package com.qingguoji.mall.common.utils;

import java.math.BigDecimal;

/**
 * 字符串 / 数值工具（参考若依 StringUtils，仅保留本项目实际使用的方法）
 *
 * @author qingguoji
 */
public class StringUtils {

    /** 手机号：11 位，1 开头 */
    private static final String PHONE_REGEX = "^1\\d{10}$";

    private StringUtils() {}

    public static boolean isEmpty(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return isEmpty(str);
    }

    public static boolean isNotBlank(String str) {
        return !isEmpty(str);
    }

    /** 是否合法手机号 */
    public static boolean isPhone(String phone) {
        return isNotEmpty(phone) && phone.matches(PHONE_REGEX);
    }

    /** 取默认值 */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /** 金额保留两位小数（四舍五入） */
    public static BigDecimal scale2(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /** 手机号脱敏：138****1111 */
    public static String maskPhone(String phone) {
        if (isEmpty(phone) || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
