package com.nexon.nutriai.controller;

import com.nexon.nutriai.constant.HttpHeaderConstant;
import com.nexon.nutriai.pojo.ChatHistory;
import com.nexon.nutriai.service.RecipeService;
import com.nexon.nutriai.util.UUIDUtil;
import com.nexon.nutriai.util.WebFluxUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/web/recipe")
@RequiredArgsConstructor
public class RecipeController extends BaseController {

    private final RecipeService recipeService;

    @GetMapping(value = "/recommend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> recommend(String question, ServerWebExchange exchange) {
        String phone = WebFluxUtil.getPhone(exchange);
        String chatId = WebFluxUtil.getChatId(exchange);
        if (chatId == null) {
            chatId = phone + "-RR-" + UUIDUtil.generateShortUUID(16);
            // 通过响应头返回新生成的 chatId
            exchange.getResponse().getHeaders().set(HttpHeaderConstant.RESPONSE_HEADER_CHAT_ID, chatId);
        }

        exchange.getResponse().getHeaders().set(HttpHeaderConstant.RESPONSE_HEADER_MODEL, recipeService.getChatModel());

        return recipeService.recommendRecipe(phone, question, chatId);
    }

    @GetMapping(value = "/getChatHistory", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatHistory> getChatHistory(String chatId) {

        return recipeService.getChatHistoryStream(chatId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "Data " + i + " at " + System.currentTimeMillis())
                .take(10);
    }
}
