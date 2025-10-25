package com.nexon.nutriai.config.sse.transformer;

import com.nexon.nutriai.config.sse.SseContext;
import org.springframework.stereotype.Component;

@Component
public class NoneTransformer extends AbstractSseTransformer {
    @Override
    protected boolean isAlreadyTargetFormat(String firstEvent, SseContext context) {
        // 默认返回 true，表示不处理
        return true;
    }
}
