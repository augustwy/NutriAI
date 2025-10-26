package com.nexon.nutriai.ports.output.ai;

import com.nexon.nutriai.domain.service.ChatHistory;
import com.nexon.nutriai.ports.output.ResultStream;

import java.util.List;

public interface ChatAPI {

    String getModel();

    /**
     * 菜谱推荐
     * @param question
     * @param chatId
     * @return
     */
    ResultStream<String> recommendRecipe(String phone, String question, String chatId);

    /**
     * 记忆获取
     * @param conversationId
     * @return
     */
    List<ChatHistory> messages(String conversationId);

    /**
     * 中断请求
     * @param chatId
     * @return
     */
    boolean interruptRequest(String chatId);
}
