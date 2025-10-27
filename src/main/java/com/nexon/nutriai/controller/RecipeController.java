package com.nexon.nutriai.controller;

import com.nexon.nutriai.constant.HttpHeaderConstant;
import com.nexon.nutriai.pojo.ChatHistory;
import com.nexon.nutriai.pojo.request.BaseRequest;
import com.nexon.nutriai.pojo.response.BaseResponse;
import com.nexon.nutriai.service.RecipeService;
import com.nexon.nutriai.util.UUIDUtil;
import com.nexon.nutriai.util.WebFluxUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/web/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping(value = "/recommend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> recommend(@RequestParam String question, ServerWebExchange exchange) {
        String phone = WebFluxUtil.getPhone(exchange);
        String chatId = WebFluxUtil.getChatId(exchange);
        if (chatId == null) {
            chatId = phone + "-RR-" + UUIDUtil.generateShortUUID(16);
            // 通过响应头返回新生成的 chatId
            exchange.getResponse().getHeaders().set(HttpHeaderConstant.RESPONSE_HEADER_CHAT_ID, chatId);
        }

        exchange.getResponse().getHeaders().set(HttpHeaderConstant.RESPONSE_HEADER_MODEL, recipeService.getChatModel());

        BaseRequest request = new BaseRequest();
        request.setPhone(phone);
        request.setChatId(chatId);
        return recipeService.recommendRecipe(request, question);
    }

    @GetMapping(value = "/getChatHistory", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatHistory> getChatHistory(@RequestParam String chatId) {

        return recipeService.getChatHistoryStream(chatId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getStream() {
        log.info("getStream");
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "Data " + i + " at " + System.currentTimeMillis())
                .take(10);
    }

    @GetMapping(value = "/test")
    public BaseResponse<Void> test() {
        return BaseResponse.success();
    }

    /**
     * 中断请求
     * @param chatId
     * @return
     */
    @PostMapping("interrupt")
    public BaseResponse<Boolean> interrupt(@RequestPart String chatId) {
        return new BaseResponse<>(true);
    }
}
