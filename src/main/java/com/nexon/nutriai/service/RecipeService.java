package com.nexon.nutriai.service;

import com.nexon.nutriai.ai.ChatAPI;
import com.nexon.nutriai.ai.common.AiTool;
import com.nexon.nutriai.ai.common.AiToolUtil;
import com.nexon.nutriai.constant.PromptConstant;
import com.nexon.nutriai.constant.annotaion.LogAnnotation;
import com.nexon.nutriai.constant.annotaion.TrackSubscription;
import com.nexon.nutriai.pojo.ChatHistory;
import com.nexon.nutriai.pojo.request.BaseAiRequest;
import com.nexon.nutriai.pojo.request.BaseRequest;
import com.nexon.nutriai.tools.TimeTools;
import com.nexon.nutriai.tools.UserTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final ChatAPI chatAPI;
    private final UserTools userTools;
    private final TimeTools timeTools;

    public String getChatModel() {
        return chatAPI.getModel();
    }

    @TrackSubscription(value = "recommend", streamIdParamName = "chatId")
    @LogAnnotation
    public Flux<String> recommendRecipe(BaseRequest request, String question) {
        BaseAiRequest baseAiRequest = new BaseAiRequest(request);
        baseAiRequest.setSystemPrompt(PromptConstant.RECOMMEND_RECIPE_SYSTEM_PROMPT);
        baseAiRequest.setContent(PromptConstant.RECOMMEND_RECIPE_USER_PROMPT);
        baseAiRequest.setContext(Map.of("phone", request.getPhone(), "question", question));

        AiTool userAiTool = AiToolUtil.build(userTools, "用户服务工具类", "userTools");
        AiTool timeAiTool = AiToolUtil.build(timeTools, "时间工具类", "timeTools");
        // 获取流式响应

        return chatAPI.chat(baseAiRequest, List.of(userAiTool, timeAiTool));
    }

    public Flux<ChatHistory> getChatHistoryStream(String chatId) {
        return chatAPI.history(chatId, (message -> {
            if (message instanceof UserMessage userMessage) {
                String text = userMessage.getText();
                int start = text.indexOf("|----------|");
                int end = text.lastIndexOf("|----------|");
                if (start != -1 && end != -1) {
                    String substring = text.substring(start + 12, end)
                            .replaceAll("\r", "")
                            .replaceAll("\n", "");
                    return new ChatHistory("user", substring);
                }
            } else if (message instanceof AssistantMessage assistantMessage) {
                return new ChatHistory("assistant", assistantMessage.getText());
            }
            return null;
        }));
    }
}
