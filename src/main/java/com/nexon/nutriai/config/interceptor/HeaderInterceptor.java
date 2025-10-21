package com.nexon.nutriai.config.interceptor;

import com.nexon.nutriai.config.properties.JwtProperties;
import com.nexon.nutriai.util.JwtUtil;
import com.nexon.nutriai.util.ThreadLocalUtil;
import com.nexon.nutriai.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@Slf4j
public class HeaderInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final JwtUtil jwtUtil;
    public HeaderInterceptor(JwtUtil jwtUtil, JwtProperties jwtProperties) {
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }
    
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        MDC.clear();

        String requestId = request.getHeader("requestId");
        if (!StringUtils.isEmpty(requestId)) {
            requestId = UUIDUtil.generateUUID();
        }

        MDC.put("REQUEST_ID", requestId);

        String chatId = request.getHeader("chatId");
        if (StringUtils.isNotEmpty(chatId)) {
            MDC.put("CHAT_ID", chatId);
            ThreadLocalUtil.THREAD_LOCAL_CHAT_ID.set(chatId);
        }

        String requestURI = request.getRequestURI();
        // 检查是否在配置的排除路径中
        if (isExcludePath(requestURI)) {
            return true; // 直接放行
        }

        // 需要鉴权的路径处理逻辑
        String token = getTokenFromRequest(request);

        if (token == null) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"缺少token\"}");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
            return false;
        }

        // 验证通过，设置用户信息
        String phone = jwtUtil.getUsernameFromToken(token);
        if (phone == null) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
            return false;
        }
        ThreadLocalUtil.THREAD_LOCAL_PHONE.set(phone);
        request.setAttribute("currentUser", phone);

        // 其他处理逻辑
        handleAdditionalLogic(request, phone);

        return true;
    }
    
    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
        // 请求完成后清理ThreadLocal，防止内存泄漏
        ThreadLocalUtil.clearAll();
    }

    private boolean isExcludePath(String requestURI) {
        return jwtProperties.getExcludePaths().stream()
                .anyMatch(path -> matchesPath(path, requestURI));
    }

    private boolean matchesPath(String pattern, String requestURI) {
        if (pattern.endsWith("/**")) {
            return requestURI.startsWith(pattern.substring(0, pattern.length() - 3));
        }
        return pattern.equals(requestURI);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        // 同上
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return request.getParameter("token");
    }

    /**
     * 其他处理逻辑
     */
    private void handleAdditionalLogic(HttpServletRequest request, String phone) {
        // 1. 记录访问日志 todo

        // 2. 检查权限（简单版）
        checkPermission(request, phone);

        // 3. 记录请求时间等
        request.setAttribute("requestTime", System.currentTimeMillis());
    }

    /**
     * 简单的权限检查示例
     */
    private void checkPermission(HttpServletRequest request, String username) {
        // 这里可以根据用户名和请求路径进行权限检查
        // 比如某些接口只允许管理员访问
        if (request.getRequestURI().startsWith("/api/admin") && !"admin".equals(username)) {
            throw new RuntimeException("权限不足");
        }
    }
}
