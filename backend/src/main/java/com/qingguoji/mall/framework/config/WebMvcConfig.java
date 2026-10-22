package com.qingguoji.mall.framework.config;

import com.qingguoji.mall.framework.security.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 通用 Web 配置（参考若依 ResourcesConfig）
 *
 * <p>统一注册跨域与登录拦截器，替代原先独立的 CorsConfig。
 *
 * @author qingguoji
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    /** 跨域：H5(10086) 与 Web 管理端(5173) 本地开发均直连后端，生产由 Nginx 同域反代 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /** 登录拦截：仅拦截 /api 与 /admin-api，其余静态资源放行 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**", "/admin-api/**")
                .excludePathPatterns("/h2-console/**", "/error");
    }
}
