package com.nexon.nutriai.api.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.nexon.nutriai.api.ChatAPI;
import com.nexon.nutriai.config.properties.DashscopeModelProperties;
import com.nexon.nutriai.constant.PromptConstant;
import com.nexon.nutriai.tools.TimeTools;
import com.nexon.nutriai.tools.UserTools;
import com.nexon.nutriai.repository.H2ChatMemoryRepository;
import com.nexon.nutriai.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DashscopeChatAPI implements ChatAPI {

    private final ChatClient dashScopeChatClient;
    private final ChatMemory messageWindowChatMemory;
    private final UserTools userTools;
    private final TimeTools timeTools;


    public DashscopeChatAPI(ChatModel chatModel, DashscopeModelProperties modelListProperties, H2ChatMemoryRepository chatMemoryRepository, UserTools userTools, TimeTools timeTools) {
        // 构造 ChatMemoryRepository 和 ChatMemory
        this.messageWindowChatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(100)
                .build();
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                // 注册Advisor
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build())
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).withModel(modelListProperties.getChat()).build())
                .build();

        this.userTools = userTools;
        this.timeTools = timeTools;
    }

    @Override
    public Flux<String> recommendRecipe(String question, String chatId) {
        String phone = ThreadLocalUtil.getPhone();
        return dashScopeChatClient.prompt(new Prompt(new SystemMessage(PromptConstant.RECOMMEND_RECIPE_SYSTEM_PROMPT),
                        new UserMessage(PromptConstant.RECOMMEND_RECIPE_USER_PROMPT_TEMPLATE.render(Map.of("phone", phone, "question", question)))))
                .tools(userTools, timeTools)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, phone + "-RR-" + chatId))
                .stream().content();
    }

    @Override
    public List<Message> messages(String conversationId) {
        return messageWindowChatMemory.get(conversationId);
    }
}
