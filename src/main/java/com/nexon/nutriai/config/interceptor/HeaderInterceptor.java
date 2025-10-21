package com.nexon.nutriai.config.interceptor;

import com.nexon.nutriai.util.ThreadLocalUtil;
import com.nexon.nutriai.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class HeaderInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        MDC.clear();
        // 从请求头中获取参数并保存到ThreadLocal
        String phone = request.getHeader("phone");
        if (phone != null) {
            ThreadLocalUtil.THREAD_LOCAL_PHONE.set(phone);
        }
        
        // 可以保存其他需要的请求头参数
        String chatId = request.getHeader("chatId");
        if (StringUtils.isEmpty(chatId)) {
            chatId = UUIDUtil.generateUUID();
        }
        MDC.put("CHAT_ID", chatId);
        ThreadLocalUtil.THREAD_LOCAL_CHAT_ID.set(chatId);

        log.info("请求: {}, chatId: {}", request.getRequestURL(), chatId);
        return true;
    }
    
    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) {
        // 请求完成后清理ThreadLocal，防止内存泄漏
        ThreadLocalUtil.clearAll();
    }
}
