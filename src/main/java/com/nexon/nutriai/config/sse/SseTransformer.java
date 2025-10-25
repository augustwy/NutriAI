package com.nexon.nutriai.config.sse;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

public interface SseTransformer {

    /**
     * 将原始的 DataBuffer 流转换为特定格式的 SSE 流。
     *
     * @param originalFlux 原始数据流
     * @param context      转换所需的上下文信息
     * @return 转换后的 SSE 数据流
     */
    Flux<DataBuffer> transform(Flux<DataBuffer> originalFlux, SseContext context);
}
