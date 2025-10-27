package com.nexon.nutriai.ai.common;

import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.nexon.nutriai.pojo.request.BaseAiRequest;
import okhttp3.internal.ws.RealWebSocket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.ArrayList;
import java.util.List;

public interface AiAPI {

    /**
     * 获取模型名称
     * @return
     */
    String getModel();

    /**
     * 构建消息列表
     * @param request
     * @return
     */
    default List<Message> buildMessages(BaseAiRequest request) {
        List<Message> messages = new ArrayList<>();
        if (StringUtils.isNotEmpty(request.getContent())) {
            // 如果存在上下文参数，需要构造提示词模板
            String userPrompt = null;
            if (null != request.getContext() && !request.getContext().isEmpty()) {
                PromptTemplate template = PromptTemplate.builder()
                        .template(request.getContent())
                        .build();
                userPrompt = template.render(request.getContext());
            } else {
                userPrompt = request.getContent();
            }
            UserMessage userMessage = UserMessage.builder().text(userPrompt).build();

            messages.add(userMessage);
        }

        if (StringUtils.isNotEmpty(request.getSystemPrompt())) {
            SystemMessage systemMessage = new SystemMessage(request.getSystemPrompt());

            messages.add(systemMessage);
        }
        return messages;
    }
}
