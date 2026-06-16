package com.waterdelivery.config;

import com.waterdelivery.common.BizException;
import com.waterdelivery.common.CurrentContext;
import com.waterdelivery.common.RequirePermission;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/public") || uri.equals("/error") || uri.startsWith("/doc.html")
                || uri.startsWith("/swagger") || uri.startsWith("/v3/api-docs")) {
            return true;
        }
        if (uri.equals("/api/user/auth/login")
                || uri.equals("/api/user/auth/register")
                || uri.equals("/api/admin/auth/login")) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BizException("请先登录");
        }
        try {
            CurrentContext.TokenPayload payload = jwtUtil.parse(token.substring(7));
            if (uri.startsWith("/api/user") && !"USER".equals(payload.getType())) {
                throw new BizException("无效的用户登录状态");
            }
            if (uri.startsWith("/api/admin") && !"ADMIN".equals(payload.getType())) {
                throw new BizException("无效的管理员登录状态");
            }
            CurrentContext.set(payload);
            RequirePermission permission = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequirePermission.class);
            if (permission == null) {
                permission = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequirePermission.class);
            }
            if (permission != null && !CurrentContext.getPermissions().contains(permission.value())) {
                throw new BizException("没有操作权限");
            }
            return true;
        } catch (JwtException ex) {
            throw new BizException("登录已过期，请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentContext.clear();
    }
}
