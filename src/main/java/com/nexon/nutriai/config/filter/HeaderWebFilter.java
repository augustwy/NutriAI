package com.nexon.nutriai.config.filter;

import com.nexon.nutriai.config.properties.JwtProperties;
import com.nexon.nutriai.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(1)
public class HeaderWebFilter implements WebFilter {

    @Value("${app.env:PROD}")
    private String env;

    private final JwtProperties jwtProperties;
    private final JwtUtil jwtUtil;

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String requestURI = request.getPath().value();

        // 1. 检查排除路径
        if (isExcludePath(requestURI)) {
            return chain.filter(exchange);
        }

        // 2. 从请求头获取信息
        String phone = request.getHeaders().getFirst("phone");
        String token = getTokenFromRequest(request);


        // 3. 根据环境决定是否进行鉴权
        if ("DEV".equals(env)) {
            return doFilter(exchange, chain);
        }

        // 4. JWT 验证逻辑
        if (StringUtils.isEmpty(token)) {
            return handleUnauthorized(response, "no token");
        }

        // 5. 将阻塞的 JWT 验证调用包装在弹性线程池中执行
        return Mono.fromCallable(() -> {
                    // 这里是阻塞调用
                    boolean isValid = jwtUtil.validateToken(token) && !jwtUtil.isRefreshToken(token);
                    if (!isValid) {
                        throw new IllegalArgumentException("Invalid token");
                    }
                    // 验证通过，获取用户信息
                    String tokenPhone = jwtUtil.getSubjectFromToken(token);
                    if (phone == null || !phone.equals(tokenPhone)) {
                        throw new IllegalArgumentException("Token phone mismatch");
                    }
                    return true; // 验证成功
                })
                .subscribeOn(Schedulers.boundedElastic()) // 在弹性线程池中执行
                .flatMap(_ -> chain.filter(exchange)) // 验证通过，继续执行后续过滤器
                .onErrorResume(ex -> { // 捕获验证过程中的任何异常
                    log.error("JWT validation failed: {}", ex.getMessage());
                    return handleUnauthorized(response, "Unauthorized");
                });
    }

    private Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String phone = request.getHeaders().getFirst("phone");
        String chatId = request.getHeaders().getFirst("chatId");

        return chain.filter(exchange).contextWrite(ctx -> {
            if (StringUtils.isNotEmpty(phone)) {
                ctx.put("currentUser", phone);
            }
            if (StringUtils.isNotEmpty(chatId)) {
                ctx.put("chatId", chatId);
            }
            return ctx;
        });
    }

    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        String body = String.format("{\"code\":401,\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private boolean isExcludePath(String requestURI) {
        return jwtProperties.excludePaths().stream()
                .anyMatch(path -> matchesPath(path, requestURI));
    }

    private boolean matchesPath(String pattern, String requestURI) {
        if (pattern.endsWith("/**")) {
            return requestURI.startsWith(pattern.substring(0, pattern.length() - 3));
        }
        return pattern.equals(requestURI);
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 这里获取的是 刷新token
        return request.getQueryParams().getFirst("token");
    }
}
