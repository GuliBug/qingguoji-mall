package com.qingguoji.mall.common.constant;

/**
 * 返回状态码（参考若依 HttpStatus）
 *
 * @author qingguoji
 */
public class HttpStatus {

    /** 操作成功 */
    public static final int SUCCESS = 200;

    /** 对象创建成功 */
    public static final int CREATED = 201;

    /** 请求已经被接受 */
    public static final int ACCEPTED = 202;

    /** 操作失败 */
    public static final int ERROR = 500;

    /** 未登录或登录已过期 */
    public static final int UNAUTHORIZED = 401;

    /** 访问权限不足 */
    public static final int FORBIDDEN = 403;

    /** 资源不存在 */
    public static final int NOT_FOUND = 404;

    private HttpStatus() {}
}
