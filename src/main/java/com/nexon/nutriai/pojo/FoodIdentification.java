package com.nexon.nutriai.pojo;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FoodIdentification {
    private List<Food> foods;
    private List<Ingredient> ingredients;

    @Getter
    @Setter
    public static class Food {
        private String name;
        private String cookingMethod;
        private double weight;

        @NotNull
        public String toString() {
            return name + ":" + weight + "g";
        }
    }

    @Getter
    @Setter
    public static class Ingredient {
        private String name;
        private String food;
        private String proportion;

        @NotNull
        public String toString() {
            return name + "在" + food + "中占" + proportion + "%";
        }
    }

    @NotNull
    public String toString() {
        return "食物=" + foods + ", 食材=" + ingredients;
    }

    @NonNull
    public Map<String, List<Ingredient>> getIngredientsMap() {
        Map<String, List<Ingredient>> ingredientsMap = new java.util.HashMap<>();
        for (Ingredient ingredient : ingredients) {
            ingredientsMap.putIfAbsent(ingredient.getFood(), new java.util.ArrayList<>());
            ingredientsMap.get(ingredient.getFood()).add(ingredient);
        }
        return ingredientsMap;
    }
}
