package com.nexon.nutriai.config.sse.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nexon.nutriai.config.sse.SseContext;
import com.nexon.nutriai.pojo.response.OpenAiSseChunk;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class OpenAiSseTransformer extends AbstractSseTransformer {

    @Override
    protected boolean isAlreadyTargetFormat(String firstEvent, SseContext context) {
        // 判断是否包含 OpenAI 特有的 "object" 字段
        // 使用 contains 是一种简单高效的模糊判断，避免了完整的 JSON 解析
        return firstEvent.contains("\"object\":\"chat.completion.chunk\"");
    }

    @Override
    protected Flux<DataBuffer> doTransform(Flux<DataBuffer> originalFlux, SseContext context) {
        return originalFlux
                .map(this::dataBufferToString)
                .map(this::sanitizeSsePayload)
                .map(json -> {
                    OpenAiSseChunk chunk = new OpenAiSseChunk(
                            context.getChatId(), "chat.completion.chunk", System.currentTimeMillis() / 1000,
                            context.getModel(), List.of(new OpenAiSseChunk.Choice(0, new OpenAiSseChunk.Delta(json), null))
                    );
                    try {
                        String sseData = "data:" + context.getObjectMapper().writeValueAsString(chunk) + "\n\n";
                        return context.getBufferFactory().wrap(sseData.getBytes(StandardCharsets.UTF_8));
                    } catch (JsonProcessingException e) {
                        String errorData = "data:{\"error\": \"Failed to serialize chunk\"}\n\n";
                        return context.getBufferFactory().wrap(errorData.getBytes(StandardCharsets.UTF_8));
                    }
                })
                .concatWithValues(context.getBufferFactory().wrap("data: [DONE]\n\n".getBytes(StandardCharsets.UTF_8)));
    }
}
