package com.nexon.nutriai.api.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.nexon.nutriai.api.ChatAPI;
import com.nexon.nutriai.config.properties.DashscopeModelProperties;
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

    private final int MAX_MESSAGES = 100;

    private final ChatClient dashScopeChatClient;
    private final DashscopeModelProperties modelListProperties;

    private static final String DEFAULT_PROMPT = """
            你是一个精通多种菜系的五星级大厨AI助手，请根据用户提问提供专业的烹饪指导！
            
            ## 角色定位
            - 专业烹饪顾问，具备丰富的菜系知识和烹饪技巧
            - 能够根据用户需求推荐合适的菜肴和提供详细食谱
            
            ## 核心技能
            1. 菜肴推荐：根据用户口味、食材或场合推荐合适菜品
            2. 食谱指导：提供详细烹饪步骤、食材清单和技巧要点
            3. 烹饪答疑：解答用户在烹饪过程中遇到的具体问题
            
            ## 交互原则
            - 专注于烹饪相关话题，对无关问题要友好地引导回烹饪主题
            - 在用户未明确指定菜肴前，仅提供食材建议和菜品方向指引
            - 回答应简洁明了，步骤清晰，适合逐步流式输出
            - 保持专业且亲和的语调，像真实大厨一样提供指导
            
            ## 输出格式要求
            - 使用清晰的标题和分段
            - 重要信息用项目符号或编号突出
            - 逐步引导用户完成烹饪过程
            - 支持流式响应，确保每段信息完整且有用
            """;


    public DashscopeChatAPI(ChatModel chatModel, DashscopeModelProperties modelListProperties, H2ChatMemoryRepository chatMemoryRepository) {
        this.modelListProperties = modelListProperties;

        // 构造 ChatMemoryRepository 和 ChatMemory
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(MAX_MESSAGES)
                .build();
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                // 注册Advisor
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).withModel(modelListProperties.getChat()).build())
                .build();
    }

    @Override
    public Flux<String> chatRecipe(String question, String chatId) {
        return dashScopeChatClient.prompt(new Prompt(new SystemMessage(DEFAULT_PROMPT), new UserMessage(question)))
                .advisors(
                        a -> a.param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .stream().content();
    }
}
