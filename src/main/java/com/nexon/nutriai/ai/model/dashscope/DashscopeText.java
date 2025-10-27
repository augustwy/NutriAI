package com.nexon.nutriai.ai.model.dashscope;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.ai.TextAPI;
import com.nexon.nutriai.ai.common.AiTool;
import com.nexon.nutriai.config.properties.ModelProperties;
import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.pojo.request.BaseAiRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Dashscope文本API实现类
 * <p>
 * 该类实现了TextAPI接口，用于处理基于Dashscope的文本分析功能。
 * 支持流式和非流式两种文本分析方式，并支持工具调用。
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.models.providers.dashscope.text")
public class DashscopeText implements TextAPI {

    private final ChatClient dashScopeChatClient;

    private final String model;

    /**
     * 构造函数
     * <p>
     * 初始化Dashscope文本API客户端和模型配置。
     *
     * @param chatModel       聊天模型
     * @param modelProperties 模型配置属性
     */
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

    /**
     * 文本分析（流式输出）
     * <p>
     * 使用Dashscope文本模型对输入内容进行分析，以流式方式返回结果。
     * 支持工具调用，可以动态注册AI工具。
     *
     * @param request 基础AI请求参数
     * @param tools   AI工具列表
     * @return 流式的分析结果
     */
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

    /**
     * 文本分析（非流式输出）
     * <p>
     * 使用Dashscope文本模型对输入内容进行分析，返回完整的分析结果。
     * 支持工具调用，可以动态注册AI工具。
     *
     * @param request 基础AI请求参数
     * @param tools   AI工具列表
     * @return 完整的分析结果
     */
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

    /**
     * 获取模型名称
     *
     * @return 当前使用的文本模型名称
     */
    @Override
    public String getModel() {
        return model;
    }
}