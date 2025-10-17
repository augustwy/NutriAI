package com.nexon.nutriai.pojo;

import java.util.List;

public record FoodRecognitionResponse(List<String> foods, NutritionInfo nutritionInfo) {
}
