package com.qingguoji.mall.framework.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录校验注解（本项目未引入 Spring Security，用拦截器实现等效能力）
 *
 * <p>标注在 Controller 类或方法上，由 {@code LoginInterceptor} 校验 X-Token。 方法上的注解优先于类上的注解。
 *
 * @author qingguoji
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequireLogin {

    /** 要求的会话类型 */
    LoginType value() default LoginType.MEMBER;
}
