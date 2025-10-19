package com.nexon.nutriai.service;

import com.nexon.nutriai.api.TextAPI;
import com.nexon.nutriai.api.VisionAPI;
import com.nexon.nutriai.pojo.FoodIdentification;
import com.nexon.nutriai.pojo.FoodRecognitionResponse;
import com.nexon.nutriai.pojo.NutritionInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FoodRecognitionService {

    private final VisionAPI visionAPI;
    private final TextAPI textAPI;

    public FoodRecognitionService(VisionAPI visionAPI, TextAPI textAPI) {
        this.visionAPI = visionAPI;
        this.textAPI = textAPI;
    }

    public FoodRecognitionResponse recognizeAndAnalyze(MultipartFile image) {
        // 调用视觉API识别食物
        FoodIdentification identification = visionAPI.analyzeFoodImage(image);
        if (identification == null) {
            return new FoodRecognitionResponse("", new NutritionInfo());
        }

        // 调用文本API获取营养信息
        NutritionInfo nutritionInfo = textAPI.calculateNutrition(identification);

        return new FoodRecognitionResponse("", nutritionInfo);
    }
}
