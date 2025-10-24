package com.nexon.nutriai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexon.nutriai.pojo.ChatHistory;
import com.nexon.nutriai.pojo.response.OpenAiSseChunk;
import com.nexon.nutriai.service.RecipeService;
import com.nexon.nutriai.util.UUIDUtil;
import com.nexon.nutriai.util.WebFluxUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/web/recipe")
@RequiredArgsConstructor
public class RecipeController extends BaseController {

    private final RecipeService recipeService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/recommend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> recommend(String question, ServerWebExchange exchange) {
        String phone = WebFluxUtil.getPhone(exchange);
        String chatId = WebFluxUtil.getChatId(exchange);
        if (chatId == null) {
            chatId = phone + "-RR-" + UUIDUtil.generateShortUUID(16);
        }

        return recipeService.recommendRecipe(phone, question, chatId);
    }

    @PostMapping(value = "/getChatHistory", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatHistory> getChatHistory(String chatId) {

        return recipeService.getChatHistoryStream(chatId);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .take(10)
                .map(sequence -> {
                    // 1. 创建 DTO 实例，构建数据
                    OpenAiSseChunk chunk = new OpenAiSseChunk(
                            "chatcmpl-" + sequence,
                            "chat.completion.chunk",
                            System.currentTimeMillis() / 1000,
                            "", // 模拟一个模型名
                            List.of(new OpenAiSseChunk.Choice(
                                    0,
                                    new OpenAiSseChunk.Delta("Data " + sequence + " at " + System.currentTimeMillis() + "\n"),
                                    null // 流式传输中，非最后一个块的 finish_reason 为 null
                            ))
                    );

                    try {
                        // 2. 使用 ObjectMapper 将 DTO 序列化为 JSON 字符串
                        String jsonData = objectMapper.writeValueAsString(chunk);

                        // 3. 构建 ServerSentEvent，注意这里直接传入 jsonData
                        return ServerSentEvent.builder(jsonData).build();

                    } catch (JsonProcessingException e) {
                        // 在生产环境中，应该有更完善的错误处理
                        // 这里返回一个错误信息事件
                        return ServerSentEvent.builder("{\"error\": \"Failed to serialize data\"}").build();
                    }
                })
                // 4. 在流结束后，添加 [DONE] 事件
                .concatWithValues(ServerSentEvent.builder("[DONE]").build());
    }

}
