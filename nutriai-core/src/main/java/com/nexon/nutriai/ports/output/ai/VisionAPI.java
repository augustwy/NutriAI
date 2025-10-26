package com.nexon.nutriai.ports.output.ai;

import com.nexon.nutriai.domain.service.FoodIdentification;

public interface VisionAPI {

    /**
     * 图片识别
     * @param filePath
     * @return
     */
    FoodIdentification analyzeFoodImage(String filePath, String phone);
}
