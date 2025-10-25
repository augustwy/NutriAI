package com.nexon.nutriai.config.filter;

import com.nexon.nutriai.config.properties.JwtProperties;
import com.nexon.nutriai.constant.HttpHeaderConstant;
import com.nexon.nutriai.util.JwtUtil;
import com.nexon.nutriai.util.WebFluxUtil;
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
import org.springframework.web.server.ResponseStatusException;
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

        // 2. 从请求中获取 Token
        String token = getTokenFromRequest(request);

        // 3. 根据环境决定是否进行鉴权
        if ("DEV".equals(env)) {
            // DEV 模式下，可以从请求头模拟一个用户
            String mockPhone = request.getHeaders().getFirst(HttpHeaderConstant.REQUEST_HEADER_USER_PHONE);
            if (StringUtils.isNotEmpty(mockPhone)) {
                exchange.getAttributes().put(WebFluxUtil.CURRENT_USER_ATTR, mockPhone);
            }
            return chain.filter(exchange);
        }

        // 4. JWT 验证逻辑
        if (StringUtils.isEmpty(token)) {
            return handleUnauthorized(response, "no token");
        }

        // 5. 【核心改造】从 JWT 中解析用户信息，而不是从请求头
        return validateJwtToken(token)
                .flatMap(phone -> {
                    // 将从 JWT 中解析出的 phone 放入上下文
                    exchange.getAttributes().put(WebFluxUtil.CURRENT_USER_ATTR, phone);

                    // chatId 仍然可以从请求头获取，因为它不是用户身份标识
                    String chatId = request.getHeaders().getFirst(HttpHeaderConstant.REQUEST_HEADER_CHAT_ID);
                    if (StringUtils.isNotEmpty(chatId)) {
                        exchange.getAttributes().put(WebFluxUtil.CHAT_ID_ATTR, chatId);
                    }
                    return chain.filter(exchange);
                });
    }

    private Mono<String> validateJwtToken(String token) {
        return Mono.fromCallable(() -> {
                    if (!jwtUtil.validateToken(token) || jwtUtil.isRefreshToken(token)) {
                        throw new IllegalArgumentException("Invalid or refresh token");
                    }
                    return jwtUtil.getSubjectFromToken(token);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(ex -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized", ex));
    }

    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        String body = String.format("{\"code\":%d,\"message\":\"%s\"}", HttpStatus.UNAUTHORIZED.value(), message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private boolean isExcludePath(String requestURI) {
        return jwtProperties.excludePaths().stream()
                .anyMatch(path -> matchesPath(path, requestURI));
    }

    private boolean matchesPath(String pattern, String requestURI) {
        String uri = extractPathAfterSecondSlash(requestURI);
        if (pattern.endsWith("/**")) {
            return uri.startsWith(pattern.substring(0, pattern.length() - 3));
        }
        return pattern.equals(uri);
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 这里获取的是 刷新token
        return request.getQueryParams().getFirst("token");
    }

    private String extractPathAfterSecondSlash(String requestURI) {
        if (requestURI == null || requestURI.isEmpty()) {
            return requestURI;
        }

        // 找到第一个斜杠的位置
        int firstSlashIndex = requestURI.indexOf('/');
        if (firstSlashIndex == -1) {
            return requestURI;
        }

        // 找到第二个斜杠的位置
        int secondSlashIndex = requestURI.indexOf('/', firstSlashIndex + 1);
        if (secondSlashIndex == -1) {
            return ""; // 没有第二个斜杠，返回空字符串
        }

        // 返回第二个斜杠后的内容
        return requestURI.substring(secondSlashIndex + 1);
    }
}
