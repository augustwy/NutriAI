package com.nexon.nutriai.ai;

import com.nexon.nutriai.ai.common.AiAPI;
import com.nexon.nutriai.ai.common.AiTool;
import com.nexon.nutriai.pojo.ChatHistory;
import com.nexon.nutriai.pojo.request.BaseAiRequest;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

public interface ChatAPI extends AiAPI {

    /**
     * 聊天
     * @param baseAiRequest 提示词
     * @param tools 工具列表
     * @return 响应结果
     */
    Flux<String> chat(BaseAiRequest baseAiRequest, List<AiTool> tools);

    /**
     * 记忆获取
     * @param chatId
     * @return
     */
    Flux<ChatHistory> history(String chatId, Function<Message, ChatHistory> mapper);
}
