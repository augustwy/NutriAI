package com.nexon.nutriai.ai.model.dashscope;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.ai.VisionAPI;
import com.nexon.nutriai.config.properties.ModelProperties;
import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.pojo.request.AiVisionRequest;
import com.nexon.nutriai.pojo.request.BaseAiRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
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

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.models.providers.dashscope.vision")
public class DashscopeVision implements VisionAPI {

    private final ChatClient dashScopeChatClient;

    private final String model;

    public DashscopeVision(ChatModel chatModel, ModelProperties modelProperties) {
        // 构造时，可以设置 ChatClient 的参数
        // {@link org.springframework.ai.chat.client.ChatClient};
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build()).build();

        this.model = modelProperties.providers().get("dashscope").vision();
    }

    @Override
    public List<Message> buildMessages(BaseAiRequest request) {
        List<Message> messages = VisionAPI.super.buildMessages(request);
        AiVisionRequest aiVisionRequest = (AiVisionRequest) request;
        if (!CollectionUtils.isEmpty(messages)) {
            ListIterator<Message> iterator = messages.listIterator();
            while (iterator.hasNext()) {
                Message message = iterator.next();
                // 覆盖默认的参数设置
                if (message instanceof UserMessage userMessage) {
                    // 构造媒体列表
                    List<Media> mediaList = new ArrayList<>();
                    if (!CollectionUtils.isEmpty(aiVisionRequest.getUris())) {
                        for (URI uri : aiVisionRequest.getUris()) {
                            mediaList.add(new Media(MimeTypeUtils.IMAGE_JPEG, uri));
                        }
                    } else if (!CollectionUtils.isEmpty(aiVisionRequest.getFilePaths())) {
                        for (String filePath : aiVisionRequest.getFilePaths()) {
                            mediaList.add(new Media(MimeTypeUtils.IMAGE_JPEG, new FileSystemResource(filePath)));
                        }
                    } else if (!CollectionUtils.isEmpty(aiVisionRequest.getInputStreams())) {
                        for (InputStream inputStream : aiVisionRequest.getInputStreams()) {
                            mediaList.add(new Media(MimeTypeUtils.IMAGE_JPEG, new InputStreamResource(inputStream)));
                        }
                    }

                    if (mediaList.isEmpty()) {
                        throw new NutriaiException(ErrorCode.IMAGE_EMPTY);
                    }

                    // 如果存在上下文参数，需要构造提示词模板
                    String userPrompt;
                    if (null != request.getContext() && !request.getContext().isEmpty()) {
                        PromptTemplate template = PromptTemplate.builder()
                                .template(request.getContent())
                                .build();
                        userPrompt = template.render(request.getContext());
                    } else {
                        userPrompt = request.getContent();
                    }
                    userMessage = UserMessage.builder().media(mediaList).text(userPrompt).build();
                    userMessage.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);

                    iterator.set(userMessage);
                }
            }
        }

        return messages;
    }

    @Override
    public <T> T imageAnalyze(AiVisionRequest request, Class<T> clazz) {
        log.info("imageAnalyze request: {}", JSONObject.toJSONString(request));

        Prompt chatPrompt = new Prompt(buildMessages(request),
                DashScopeChatOptions.builder().withModel(model)  // 使用视觉模型
                        .withMultiModel(true)             // 启用多模态
                        .withVlHighResolutionImages(true) // 启用高分辨率图片处理
                        .withTemperature(0.7).build());
        log.debug("imageAnalyze request: {}", chatPrompt);
        T t = dashScopeChatClient.prompt(chatPrompt).call().entity(clazz);
        log.info("imageAnalyze response: {}", JSONObject.toJSONString(t));
        return t;
    }

    @Override
    public String getModel() {
        return model;
    }
}
