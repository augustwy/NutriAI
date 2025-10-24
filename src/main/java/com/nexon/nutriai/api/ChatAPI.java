package com.nexon.nutriai.api;

import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatAPI {

    String getModel();

    /**
     * 菜谱推荐
     * @param question
     * @param chatId
     * @return
     */
    Flux<String> recommendRecipe(String phone, String question, String chatId);

    /**
     * 记忆获取
     * @param conversationId
     * @return
     */
    List<Message> messages(String conversationId);
}
