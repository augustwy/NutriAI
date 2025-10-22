package com.nexon.nutriai.controller;

import com.nexon.nutriai.pojo.BaseResponse;
import com.nexon.nutriai.service.RecipeService;
import com.nexon.nutriai.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/web/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/recommend")
    public Flux<String> recommend(String question, HttpServletResponse response) {
        String chatId = ThreadLocalUtil.getChatId();

        // 设置响应头
        response.setHeader("chatId", chatId);
        response.setCharacterEncoding("UTF-8");
        return recipeService.recommendRecipe(question, chatId);
    }

    @GetMapping("/messages")
    public BaseResponse<List<Message>> messages(String chatId) {
        return new BaseResponse<>(recipeService.messages(chatId));
    }
}
