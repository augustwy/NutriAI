package com.nexon.nutriai.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NutritionAnalyzeResult {

    private List<FoodDetail> foods;
    private Summary summary;

    @Getter
    @Setter
    public static class FoodDetail {
        private String name;
        private double weight;
        private double totalCalorie;
        private double totalProtein;
        private double totalFat;
        private double totalCarbohydrate;
        private String analysis;


    }

    @Getter
    @Setter
    public static class Summary {
        private double totalCalorie;
        private double totalProtein;
        private double totalFat;
        private double totalCarbohydrate;
        private String overallAnalysis;
        private List<String> recommendations;
    }
}
