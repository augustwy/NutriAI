package com.nexon.nutriai.api;

import com.nexon.nutriai.pojo.FoodIdentification;
import com.nexon.nutriai.pojo.NutritionAnalyzeResult;
import com.nexon.nutriai.pojo.NutritionInfo;
import org.springframework.stereotype.Service;

@Service
public interface TextAPI {

    /**
     * 计算食物种的营养元素
     * @param identification
     * @return
     */
    NutritionInfo calculateNutrition(FoodIdentification identification);

    /**
     * 分析营养元素
     * @param identification
     * @param nutritionInfo
     * @return
     */
    String analyzeNutrition(FoodIdentification identification, NutritionInfo nutritionInfo);
}
