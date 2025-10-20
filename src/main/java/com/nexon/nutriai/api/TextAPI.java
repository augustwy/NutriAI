package com.nexon.nutriai.api;

import com.nexon.nutriai.pojo.FoodIdentification;
import org.springframework.stereotype.Service;

@Service
public interface TextAPI {

    /**
     * 分析营养元素
     * @param identification
     * @return
     */
    String generateNutritionReport(FoodIdentification identification);
}
