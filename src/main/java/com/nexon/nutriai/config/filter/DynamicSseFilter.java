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

        String chatId = determineAndSetChatId(exchange);
        String model = exchange.getResponse().getHeaders().getFirst(HttpHeaderConstant.RESPONSE_HEADER_MODEL);

        // 创建转换上下文
        SseContext context = new SseContext(chatId, model, exchange.getResponse().bufferFactory(), new ObjectMapper());

        // 从工厂获取转换器
        SseTransformer transformer = transformerFactory.getTransformer(responseFormat);

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.TEXT_EVENT_STREAM);
        response.getHeaders().set(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.getHeaders().set(HttpHeaders.CONNECTION, "keep-alive");

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (!(body instanceof Flux)) {
                    return super.writeWith(body);
                }
                // 委托给转换器处理
                return super.writeWith(transformer.transform((Flux<DataBuffer>) body, context));
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                Flux<DataBuffer> flattenedFlux = Flux.from(body).flatMap(p -> p);
                // 同样委托给转换器处理
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
