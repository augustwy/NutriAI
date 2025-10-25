package com.nexon.nutriai.config.sse.transformer;

import com.nexon.nutriai.config.sse.SseTransformer;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * SSE 转换器工厂，负责根据格式名称创建对应的转换器实例。
 */
@Component
public class SseTransformerFactory {

    private final Map<String, SseTransformer> transformerMap;

    // 通过构造器注入所有实现了 SseTransformer 接口的 Bean
    public SseTransformerFactory(OpenAiSseTransformer openAiTransformer, BasicSseTransformer basicTransformer) {
        this.transformerMap = Map.of(
                "OpenAI", openAiTransformer,
                "Basic", basicTransformer
        );
    }

    /**
     * 根据格式名称获取转换器。
     * @param formatName 格式名称（如 "OpenAI", "Basic"）
     * @return 对应的转换器实例，如果未找到则返回 空 作为默认值。
     */
    public SseTransformer getTransformer(String formatName) {
        return transformerMap.getOrDefault(formatName, transformerMap.get("None"));
    }
}
