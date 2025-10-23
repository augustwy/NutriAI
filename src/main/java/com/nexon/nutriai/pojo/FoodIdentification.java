package com.nexon.nutriai.pojo;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
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

    public Map<String, Object> toTemplateParameters() {
        StringBuilder foodList = new StringBuilder();
        if (this.getFoods() != null && !this.getFoods().isEmpty()) {
            for (Food food : this.getFoods()) {
                foodList.append("- ")
                        .append(food.getName())
                        .append(" ")
                        .append(food.getWeight())
                        .append("克 (烹饪方式: ")
                        .append(food.getCookingMethod())
                        .append(")\n");
            }
        }

        StringBuilder ingredientDetails = new StringBuilder();
        if (this.getIngredients() != null && !this.getIngredients().isEmpty()) {
            for (Ingredient ingredient : this.getIngredients()) {
                ingredientDetails.append("- ")
                        .append(ingredient.getName())
                        .append(": 属于")
                        .append(ingredient.getFood())
                        .append(" (占比")
                        .append(ingredient.getProportion())
                        .append(")\n");
            }
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("food_list", foodList.toString().trim());
        parameters.put("ingredient_details", ingredientDetails.toString().trim());

        return parameters;
    }

}
