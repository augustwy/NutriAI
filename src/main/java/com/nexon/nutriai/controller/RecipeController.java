package com.nexon.nutriai.controller;

import com.nexon.nutriai.service.RecipeService;
import com.nexon.nutriai.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/web/recipe")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/recommend")
    public Flux<String> recommend(String question, HttpServletResponse response) {
        String chatId = ThreadLocalUtil.getChatId();

        response.setCharacterEncoding("UTF-8");
        return recipeService.recommendRecipe(question, chatId);
    }
}
