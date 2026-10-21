package com.qingguoji.mall.common;

/** 业务异常：库存不足、状态非法等 */
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}
