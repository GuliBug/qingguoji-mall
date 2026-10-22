package com.qingguoji.mall.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingguoji.mall.common.constant.Constants;
import com.qingguoji.mall.common.core.domain.AjaxResult;
import com.qingguoji.mall.framework.security.annotation.LoginType;
import com.qingguoji.mall.framework.security.annotation.RequireLogin;
import com.qingguoji.mall.framework.web.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器（参考若依 JwtAuthenticationTokenFilter 的职责）
 *
 * <p>只对标注 {@link RequireLogin} 的方法/类生效，未标注的接口（登录、商品列表）直接放行， 避免改造过程中误伤公开接口。
 *
 * @author qingguoji
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequireLogin requireLogin = findAnnotation(handlerMethod);
        if (requireLogin == null) {
            // 公开接口（登录、商品浏览）
            return true;
        }

        String token = request.getHeader(Constants.TOKEN_HEADER);
        if (requireLogin.value() == LoginType.ADMIN) {
            String adminName = tokenService.getAdminName(token);
            if (adminName == null) {
                writeUnauthorized(response, "管理员未登录或登录已过期");
                return false;
            }
            LoginHelper.setLoginUser(LoginUser.admin(adminName));
            return true;
        }

        Long memberId = tokenService.getMemberId(token);
        if (memberId == null) {
            writeUnauthorized(response, "请先登录");
            return false;
        }
        LoginHelper.setLoginUser(LoginUser.member(memberId));
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        LoginHelper.clear();
    }

    /** 方法上的注解优先于类上的注解 */
    private RequireLogin findAnnotation(HandlerMethod handlerMethod) {
        RequireLogin annotation = handlerMethod.getMethodAnnotation(RequireLogin.class);
        return annotation != null
                ? annotation
                : handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        AjaxResult<Void> body = AjaxResult.unauthorized(message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
