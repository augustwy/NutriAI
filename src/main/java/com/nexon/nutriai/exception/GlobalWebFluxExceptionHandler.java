package com.nexon.nutriai.exception;

import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.pojo.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(-2) // 设置优先级高于默认的异常处理
public class GlobalWebFluxExceptionHandler implements ErrorWebExceptionHandler {

    private final ServerCodecConfigurer configurer;

    public GlobalWebFluxExceptionHandler(ServerCodecConfigurer configurer) {
        this.configurer = configurer;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        BaseResponse error;
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof NutriaiException) {
            NutriaiException nutriaiEx = (NutriaiException) ex;
            error = new BaseResponse(nutriaiEx.getCode(), nutriaiEx.getMessage());
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusEx = (ResponseStatusException) ex;
            status = (HttpStatus) responseStatusEx.getStatusCode();
            error = new BaseResponse(ErrorCode.UNKNOWN_ERROR, responseStatusEx.getReason());
        } else {
            log.error("handleGenericException", ex);
            error = new BaseResponse(ErrorCode.UNKNOWN_ERROR, ex.getMessage());
        }

        // 设置响应状态和内容类型
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 将错误信息转换为JSON并写入响应
        String errorMessage = "{\"code\":\"" + error.getCode() + "\",\"message\":\"" + error.getMessage() + "\"}";
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer dataBuffer = bufferFactory.wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}
