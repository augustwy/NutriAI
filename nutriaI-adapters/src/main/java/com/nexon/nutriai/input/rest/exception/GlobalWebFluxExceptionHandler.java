package com.nexon.nutriai.input.rest.exception;

import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.pojo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(-1)
public class GlobalWebFluxExceptionHandler implements ErrorWebExceptionHandler {

    public GlobalWebFluxExceptionHandler() {

    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 检查响应是否已经提交
        if (exchange.getResponse().isCommitted()) {
            return Mono.empty();
        }

        BaseResponse error;
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof NutriaiException) {
            NutriaiException nutriaiEx = (NutriaiException) ex;
            error = new BaseResponse(nutriaiEx.getCode(), nutriaiEx.getMessage());
            // 业务异常应该返回成功状态码
            status = HttpStatus.OK;
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusEx = (ResponseStatusException) ex;
            status = (HttpStatus) responseStatusEx.getStatusCode();
            error = new BaseResponse(String.valueOf(status.value()), responseStatusEx.getReason());
        } else {
            log.error("handleGenericException", ex);
            error = new BaseResponse(ErrorCode.UNKNOWN_ERROR, ex.getMessage());
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            String errorMessage = "{\"code\":\"" + error.getCode() + "\",\"message\":\"" + error.getMessage() + "\"}";
            DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
            DataBuffer dataBuffer = bufferFactory.wrap(errorMessage.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        } catch (Exception e) {
            log.error("Failed to write error response", e);
            return Mono.empty();
        }
    }
}
