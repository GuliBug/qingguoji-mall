package com.qingguoji.mall.common.exception;

import com.qingguoji.mall.common.constant.HttpStatus;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理（参考若依 GlobalExceptionHandler）
 *
 * <p>统一把异常转成 {@code {code, msg}}，HTTP 状态保持 200，由前端按 code 分支处理。
 *
 * @author qingguoji
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：直接返回异常消息 */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult<Void> handleServiceException(ServiceException e) {
        log.warn("业务异常：{}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /** 参数校验异常（@Valid） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg =
                fieldError == null
                        ? "参数校验失败"
                        : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return AjaxResult.error(msg);
    }

    /** 参数绑定异常（表单） */
    @ExceptionHandler(BindException.class)
    public AjaxResult<Void> handleBindException(BindException e) {
        String msg =
                e.getFieldErrors().stream()
                        .findFirst()
                        .map(f -> f.getField() + ": " + f.getDefaultMessage())
                        .orElse("参数校验失败");
        return AjaxResult.error(msg);
    }

    /** 缺少必填请求头（如未登录时缺 X-Token） */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public AjaxResult<Void> handleMissingHeader(MissingRequestHeaderException e) {
        return AjaxResult.unauthorized("缺少请求头 " + e.getHeaderName() + "，请先登录");
    }

    /** 兜底异常：不向前端暴露堆栈细节 */
    @ExceptionHandler(Exception.class)
    public AjaxResult<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return AjaxResult.error(HttpStatus.ERROR, "系统繁忙，请稍后重试");
    }
}
