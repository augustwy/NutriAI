package com.nexon.nutriai.pojo;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record FoodIdentification(List<String> foods, List<String> ingredients) {

    @NotNull
    public String toString() {
        return "食物=" + foods + ", 食材=" + ingredients;
    }
}
