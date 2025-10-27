package com.nexon.nutriai.ai.model.dashscope;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.jdbc.H2ChatMemoryRepository;
import com.nexon.nutriai.ai.ChatAPI;
import com.nexon.nutriai.ai.common.AiTool;
import com.nexon.nutriai.config.properties.ModelProperties;
import com.nexon.nutriai.pojo.ChatHistory;
import com.nexon.nutriai.pojo.request.BaseAiRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.models.providers.dashscope.chat")
public class DashscopeChat implements ChatAPI {

    private final ChatClient dashScopeChatClient;
    private final ChatMemory messageWindowChatMemory;

    private final String model;

    public DashscopeChat(ChatModel chatModel, ModelProperties modelProperties, H2ChatMemoryRepository h2ChatMemoryRepository) {
        this.model = modelProperties.providers().get("dashscope").chat();
        // 构造 ChatMemoryRepository 和 ChatMemory
        this.messageWindowChatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(h2ChatMemoryRepository)
                .maxMessages(100)
                .build();
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                // 注册Advisor
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build())
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).withModel(model).build())
                .build();
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public Flux<String> chat(BaseAiRequest request, List<AiTool> tools) {
        Prompt chatPrompt = new Prompt(buildMessages(request), DashScopeChatOptions.builder()
                .withModel(model)
                .build());

        // 动态注册工具
        Object[] nativeTools = new Object[0];
        if (tools != null && !tools.isEmpty()) {
            nativeTools = tools.stream()
                    .map(AiTool::getNativeTool)
                    .toArray();
        }
        return dashScopeChatClient.prompt(chatPrompt)
                .tools(nativeTools)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.getChatId()))
                .stream()
                .content();
    }


    @Override
    public Flux<ChatHistory> history(String chatId, Function<Message, ChatHistory> mapper) {
        return Flux.fromIterable(messageWindowChatMemory.get(chatId))
                .mapNotNull(message -> {
                    if (null == mapper) {
                        // 如果没有转换器，就直接输出原格式
                        return new ChatHistory(message.getMessageType().getValue(), message.getText());
                    }
                    // 按转换器输出
                    return mapper.apply(message);
                })
                .filter(Objects::nonNull);
    }
}
