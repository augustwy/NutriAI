package com.nexon.nutriai.ai;

import com.nexon.nutriai.ai.common.AiAPI;
import com.nexon.nutriai.ai.common.AiTool;
import com.nexon.nutriai.pojo.request.BaseAiRequest;
import reactor.core.publisher.Flux;

import java.util.List;

public interface TextAPI extends AiAPI {

    /**
     * 文本分析（流式输出）
     * @param baseAiRequest
     * @return
     */
    Flux<String> textAnalysis4Stream(BaseAiRequest baseAiRequest, List<AiTool> tools);

    /**
     * 纯文本分析
     * @param baseAiRequest
     * @return
     */
    default String textAnalysis(BaseAiRequest baseAiRequest, List<AiTool> tools) {
        return textAnalysis4Stream(baseAiRequest, tools).collectList().block().stream().reduce("", String::concat);
    }
}
