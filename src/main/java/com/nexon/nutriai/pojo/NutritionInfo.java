package com.nexon.nutriai.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class NutritionInfo {

    private List<Ingredient> ingredients;

    @Getter
    @Setter
    public static class Ingredient {
        private String name;
        private double calorie;
        private double protein;
        private double fat;
        private double carbohydrate;
    }

    public void addIngredient(Ingredient ingredient) {
        if (this.ingredients == null) {
            this.ingredients = new ArrayList<>();
        }
        this.ingredients.add(ingredient);
    }

    public Map<String, Ingredient> toMap() {
        return ingredients.stream().collect(Collectors.toMap(Ingredient::getName, ingredient -> ingredient));
    }
}
