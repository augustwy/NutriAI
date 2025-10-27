package com.nexon.nutriai.ai.model.dashscope;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.ai.TextAPI;
import com.nexon.nutriai.ai.VisionAPI;
import com.nexon.nutriai.ai.common.AiTool;
import com.nexon.nutriai.config.properties.ModelProperties;
import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.constant.PromptConstant;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.pojo.FoodIdentification;
import com.nexon.nutriai.pojo.request.AiVisionRequest;
import com.nexon.nutriai.pojo.request.BaseAiRequest;
import com.nexon.nutriai.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.models.providers.dashscope.text")
public class DashscopeText implements TextAPI {

    private final ChatClient dashScopeChatClient;

    private final String model;

    public DashscopeText(ChatModel chatModel, ModelProperties modelProperties) {
        // 构造时，可以设置 ChatClient 的参数
        // {@link org.springframework.ai.chat.client.ChatClient};
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build()).build();

        this.model = modelProperties.providers().get("dashscope").text();
    }

    @Override
    public Flux<String> textAnalysis4Stream(BaseAiRequest request, List<AiTool> tools) {
        log.info("textAnalysis4Stream request: {}", JSONObject.toJSONString(request));
        if (null == request.getContent()) {
            throw new NutriaiException(ErrorCode.NONE_PARAM_ERROR, "分析数据");
        }

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

        return dashScopeChatClient.prompt(chatPrompt).tools(nativeTools).stream().content();
    }

    @Override
    public String textAnalysis(BaseAiRequest request, List<AiTool> tools) {
        log.info("textAnalysis request: {}", JSONObject.toJSONString(request));
        if (null == request.getContent()) {
            throw new NutriaiException(ErrorCode.NONE_PARAM_ERROR, "分析数据");
        }

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

        return dashScopeChatClient.prompt(chatPrompt).tools(nativeTools).call().content();
    }

    @Override
    public String getModel() {
        return model;
    }
}
