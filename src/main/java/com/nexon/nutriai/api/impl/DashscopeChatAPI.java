package com.nexon.nutriai.api.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.nexon.nutriai.api.ChatAPI;
import com.nexon.nutriai.config.properties.DashscopeModelProperties;
import com.nexon.nutriai.constant.PromptConstant;
import com.nexon.nutriai.repository.H2ChatMemoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class DashscopeChatAPI implements ChatAPI {

    private final ChatClient dashScopeChatClient;


    public DashscopeChatAPI(ChatModel chatModel, DashscopeModelProperties modelListProperties, H2ChatMemoryRepository chatMemoryRepository) {
        // 构造 ChatMemoryRepository 和 ChatMemory
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(100)
                .build();
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                // 注册Advisor
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).withModel(modelListProperties.getChat()).build())
                .build();
    }

    @Override
    public Flux<String> recommendRecipe(String question, String chatId) {
        return dashScopeChatClient.prompt(new Prompt(new SystemMessage(PromptConstant.RECOMMEND_RECIPE_SYSTEM_PROMPT), new UserMessage(question)))
                .advisors(
                        a -> a.param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .stream().content();
    }
}
