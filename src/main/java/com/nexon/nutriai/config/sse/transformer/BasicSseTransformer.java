package com.nexon.nutriai.config.sse.transformer;

import com.nexon.nutriai.config.sse.SseContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

@Component
public class BasicSseTransformer extends AbstractSseTransformer {

    @Override
    protected boolean isAlreadyTargetFormat(String firstEvent, SseContext context) {
        return firstEvent.startsWith("data:") && firstEvent.endsWith("\n\n");
    }

    @Override
    protected Flux<DataBuffer> doTransform(Flux<DataBuffer> originalFlux, SseContext context) {
        return originalFlux
                .map(this::dataBufferToString)
                .map(this::sanitizeSsePayload)
                .map(json -> {
                    String sseData = "data:" + json + "\n\n";
                    return context.getBufferFactory().wrap(sseData.getBytes(StandardCharsets.UTF_8));
                })
                .concatWithValues(context.getBufferFactory().wrap("data: [DONE]\n\n".getBytes(StandardCharsets.UTF_8)));
    }
}
