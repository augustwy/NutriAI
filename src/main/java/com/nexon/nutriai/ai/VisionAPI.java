package com.nexon.nutriai.ai;

import com.nexon.nutriai.ai.common.AiAPI;
import com.nexon.nutriai.ai.common.AiTool;
import com.nexon.nutriai.pojo.request.AiVisionRequest;

import java.util.List;

public interface VisionAPI extends AiAPI {

    /**
     * 图像分析
     * @param request
     * @return
     */
    <T> T imageAnalyze(AiVisionRequest request, Class<T> clazz);

    default String imageAnalyze(AiVisionRequest request) {
        return imageAnalyze(request, String.class);
    }

}
