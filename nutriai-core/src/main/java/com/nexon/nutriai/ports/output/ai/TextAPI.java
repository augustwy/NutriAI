package com.nexon.nutriai.ports.output.ai;

import com.nexon.nutriai.domain.service.FoodIdentification;

public interface TextAPI {

    /**
     * 分析营养元素
     * @param identification
     * @return
     */
    String generateNutritionReport(FoodIdentification identification, String phone);
}
