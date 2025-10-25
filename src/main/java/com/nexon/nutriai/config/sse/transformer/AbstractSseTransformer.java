package com.nexon.nutriai.config.sse.transformer;

import com.nexon.nutriai.config.sse.SseContext;
import com.nexon.nutriai.config.sse.SseTransformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 抽象 SSE 转换器，提供通用的辅助方法和“短路”优化逻辑。
 */
public abstract class AbstractSseTransformer implements SseTransformer {

    @Override
    public Flux<DataBuffer> transform(Flux<DataBuffer> originalFlux, SseContext context) {
        // 【核心优化】缓存原始流，允许我们多次订阅（一次用于判断，一次用于处理）
        Flux<DataBuffer> cachedFlux = originalFlux.cache();

        // 窥视第一个 DataBuffer 来判断格式
        Mono<Boolean> isTargetFormatMono = cachedFlux.next()
                .map(firstBuffer -> isAlreadyTargetFormat(dataBufferToString(firstBuffer), context))
                .defaultIfEmpty(false); // 如果流为空，则不是目标格式

        // 根据判断结果，选择不同的处理路径
        return isTargetFormatMono.flatMapMany(isTarget -> {
            if (isTarget) {
                // 如果已经是目标格式，直接透传缓存的原始流
                // cachedFlux 中的所有 DataBuffer（包括第一个）都将被下游消费并正确释放
                return cachedFlux;
            } else {
                // 如果不是目标格式，则执行转换逻辑
                // doTransform 将消费 cachedFlux，并负责释放其中的 DataBuffer
                return doTransform(cachedFlux, context);
            }
        });
    }

    /**
     * 由子类实现：判断给定的第一个事件字符串是否已经是目标格式。
     * @param firstEvent 第一个 SSE 事件的字符串形式
     * @param context    转换上下文
     * @return true 如果是目标格式，否则 false
     */
    protected abstract boolean isAlreadyTargetFormat(String firstEvent, SseContext context);

    /**
     * 执行具体的转换逻辑。
     * 默认不处理，直接返回原始数据流。
     * @param originalFlux 原始数据流
     * @param context      转换上下文
     * @return 转换后的数据流
     */
    protected Flux<DataBuffer> doTransform(Flux<DataBuffer> originalFlux, SseContext context) {
        return originalFlux;
    }


    // --- 通用辅助方法 ---

    /**
     * 【关键修改】将 DataBuffer 的内容转换为字符串，但不释放 DataBuffer。
     * 这允许 DataBuffer 被 cache() 操作符重放，并由最终的消费者负责释放。
     * @param buffer 要读取的 DataBuffer
     * @return 字符串内容
     */
    protected String dataBufferToString(DataBuffer buffer) {
        // 为了不消费 buffer，我们读取它的内容，然后重置读位置
        byte[] bytes = new byte[buffer.readableByteCount()];
        // 保存原始读位置
        int originalReadPosition = buffer.readPosition();
        buffer.read(bytes);
        // 将读位置重置，以便后续消费者可以再次读取
        buffer.readPosition(originalReadPosition);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    protected String sanitizeSsePayload(String rawPayload) {
        if (StringUtils.isEmpty(rawPayload)) {
            return "";
        }
        String content = rawPayload.trim();
        if (content.endsWith("\n\n")) {
            content = content.substring(0, content.length() - 2);
        }
        content = "\n" + content;
        content = content.replaceAll("\ndata:", "\n");
        if (content.startsWith("\n")) {
            content = content.substring(1);
        }
        return content;
    }
}
