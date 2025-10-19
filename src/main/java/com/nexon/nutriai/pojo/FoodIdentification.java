package com.nexon.nutriai.pojo;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Setter
public class FoodIdentification {
    private List<Food> foods;
    private List<Ingredient> ingredients;

    @Getter
    @Setter
    public static class Food {
        private String name;
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
            return name + "在" + food + "中占" + proportion;
        }
    }

    @NotNull
    public String toString() {
        return "食物=" + foods + ", 食材=" + ingredients;
    }
}
