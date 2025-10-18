package com.nexon.nutriai.api;

import com.nexon.nutriai.pojo.FoodIdentification;
import com.nexon.nutriai.pojo.NutritionInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TextAPI {

    NutritionInfo getNutritionInfo(FoodIdentification identification);
}
