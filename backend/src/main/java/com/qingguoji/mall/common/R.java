package com.qingguoji.mall.common;

import lombok.Data;

/**
 * 统一响应体（参考若依 AjaxResult）：{code, msg, data}
 * code: 200 成功, 500 业务失败, 401 未登录
 */
@Data
public class R<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = "操作成功";
        r.data = data;
        return r;
    }

    public static R<Void> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.code = 500;
        r.msg = msg;
        return r;
    }

    public static <T> R<T> unauthorized(String msg) {
        R<T> r = new R<>();
        r.code = 401;
        r.msg = msg;
        return r;
    }
}
