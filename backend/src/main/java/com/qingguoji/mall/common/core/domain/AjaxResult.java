package com.qingguoji.mall.common.core.domain;

import com.qingguoji.mall.common.constant.HttpStatus;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 统一响应体（参考若依 AjaxResult）
 *
 * <p>JSON 结构固定为 {@code {code, msg, data}}，前端 request.js 依据 code 判定成败， 因此改造过程中不得变更字段名。
 *
 * @param <T> 业务数据类型
 * @author qingguoji
 */
@Data
public class AjaxResult<T> implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    /** 状态码：200 成功 / 500 业务失败 / 401 未登录 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 业务数据 */
    private T data;

    public AjaxResult() {}

    public AjaxResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public AjaxResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /** 操作成功，无返回数据 */
    public static <T> AjaxResult<T> success() {
        return new AjaxResult<>(HttpStatus.SUCCESS, "操作成功");
    }

    /** 操作成功，返回数据 */
    public static <T> AjaxResult<T> success(T data) {
        return new AjaxResult<>(HttpStatus.SUCCESS, "操作成功", data);
    }

    /** 操作成功，自定义提示 + 数据 */
    public static <T> AjaxResult<T> success(String msg, T data) {
        return new AjaxResult<>(HttpStatus.SUCCESS, msg, data);
    }

    /** 业务失败 */
    public static <T> AjaxResult<T> error(String msg) {
        return new AjaxResult<>(HttpStatus.ERROR, msg);
    }

    /** 业务失败，自定义状态码 */
    public static <T> AjaxResult<T> error(int code, String msg) {
        return new AjaxResult<>(code, msg);
    }

    /** 未登录 / 登录已过期 */
    public static <T> AjaxResult<T> unauthorized(String msg) {
        return new AjaxResult<>(HttpStatus.UNAUTHORIZED, msg);
    }

    /** 是否成功（供服务端内部判断，不参与序列化） */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isSuccess() {
        return code == HttpStatus.SUCCESS;
    }
}
