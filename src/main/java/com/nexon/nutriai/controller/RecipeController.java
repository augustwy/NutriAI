package com.nexon.nutriai.controller;

import com.nexon.nutriai.config.annotation.SseResponse;
import com.nexon.nutriai.pojo.ChatHistory;
import com.nexon.nutriai.service.RecipeService;
import com.nexon.nutriai.util.ThreadLocalUtil;
import com.nexon.nutriai.util.UUIDUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/web/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping(value = "/recommend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SseResponse
    public Flux<String> recommend(String question, HttpServletResponse response) {
        String phone = ThreadLocalUtil.getPhone();
        String chatId = ThreadLocalUtil.getChatId();
        if (chatId == null) {
            chatId = phone + "-RR-" + UUIDUtil.generateShortUUID(16);
        }

        // 设置响应头
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Type", "text/event-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Chat-Id", chatId);
        return recipeService.recommendRecipe(phone, question, chatId);
    }

    @PostMapping(value = "/getChatHistory", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatHistory> getChatHistory(String chatId, HttpServletResponse response) {

        // 设置响应头
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Type", "text/event-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        return recipeService.getChatHistoryStream(chatId);
    }
}
