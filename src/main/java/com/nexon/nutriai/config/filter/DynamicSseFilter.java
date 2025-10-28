package com.nexon.nutriai.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexon.nutriai.config.sse.SseContext;
import com.nexon.nutriai.config.sse.SseTransformer;
import com.nexon.nutriai.config.sse.transformer.SseTransformerFactory;
import com.nexon.nutriai.constant.HttpHeaderConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;

import java.util.UUID;

@Component
public class DynamicSseFilter implements WebFilter {

    private final SseTransformerFactory transformerFactory;

    public DynamicSseFilter(SseTransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String responseFormat = exchange.getRequest().getHeaders().getFirst(HttpHeaderConstant.REQUEST_HEADER_RESPONSE_FORMAT);
        if (responseFormat == null) {
            return chain.filter(exchange);
        }

        // 如果没有就走默认的处理器
        SseTransformer transformer = transformerFactory.getTransformer(responseFormat);
        if (transformer == null) {
            return chain.filter(exchange);
        }

        String chatId = determineAndSetChatId(exchange);
        String model = exchange.getResponse().getHeaders().getFirst(HttpHeaderConstant.RESPONSE_HEADER_MODEL);
        String contentType = exchange.getResponse().getHeaders().getFirst(HttpHeaderConstant.RESPONSE_HEADER_CONTENT_TYPE);
        if (StringUtils.isEmpty(contentType)) {
            // 默认是普通消息
            contentType = "message";
            exchange.getResponse().getHeaders().set(HttpHeaderConstant.RESPONSE_HEADER_CONTENT_TYPE, contentType);
        }

        // 创建转换上下文
        SseContext context = new SseContext(chatId, model, contentType, exchange.getResponse().bufferFactory(), new ObjectMapper());

        // 从工厂获取转换器


        ServerHttpResponse originalResponse = exchange.getResponse();

        // 【核心修改】在这里不再直接设置SSE响应头，而是延迟到确认是Flux响应时再设置
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

            // 使用一个标志来确保SSE头信息只被设置一次，防止重复操作
            private volatile boolean sseHeadersSet = false;

            /**
             * 私有方法：用于按需设置SSE头信息
             */
            private void setSseHeadersIfNeeded() {
                if (!sseHeadersSet) {
                    getDelegate().getHeaders().setContentType(MediaType.TEXT_EVENT_STREAM);
                    getDelegate().getHeaders().set(HttpHeaders.CACHE_CONTROL, "no-cache");
                    getDelegate().getHeaders().set(HttpHeaders.CONNECTION, "keep-alive");
                    sseHeadersSet = true;
                }
            }

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    // 【关键】只有当body是Flux时，才设置SSE头并进行转换
                    setSseHeadersIfNeeded();
                    return super.writeWith(transformer.transform((Flux<DataBuffer>) body, context));
                } else {
                    // 如果是普通的Mono响应，则直接传递，不做任何处理
                    return super.writeWith(body);
                }
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                // writeAndFlushWith 本身就是为流式设计的，我们将其展平后按Flux处理
                Flux<DataBuffer> flattenedFlux = Flux.from(body).flatMap(p -> p);
                setSseHeadersIfNeeded();
                return super.writeWith(transformer.transform(flattenedFlux, context));
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    /**
     * 辅助方法：统一处理 ChatId 的确定和设置
     */
    private String determineAndSetChatId(ServerWebExchange exchange) {
        String respChatId = exchange.getResponse().getHeaders().getFirst(HttpHeaderConstant.RESPONSE_HEADER_CHAT_ID);
        if (StringUtils.isNotEmpty(respChatId)) {
            return respChatId;
        }
        String reqChatId = exchange.getRequest().getHeaders().getFirst(HttpHeaderConstant.REQUEST_HEADER_CHAT_ID);
        if (StringUtils.isNotEmpty(reqChatId)) {
            exchange.getResponse().getHeaders().add(HttpHeaderConstant.RESPONSE_HEADER_CHAT_ID, reqChatId);
            return reqChatId;
        }
        String generatedChatId = "req-" + UUID.randomUUID();
        exchange.getResponse().getHeaders().add(HttpHeaderConstant.RESPONSE_HEADER_CHAT_ID, generatedChatId);
        return generatedChatId;
    }
}
