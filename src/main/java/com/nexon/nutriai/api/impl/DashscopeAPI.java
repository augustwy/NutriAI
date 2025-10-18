package com.nexon.nutriai.api.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.api.TextAPI;
import com.nexon.nutriai.api.VisionAPI;
import com.nexon.nutriai.config.ModelListProperties;
import com.nexon.nutriai.pojo.FoodIdentification;
import com.nexon.nutriai.pojo.NutritionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@Slf4j
public class DashscopeAPI implements VisionAPI, TextAPI {

    private final ChatClient dashScopeChatClient;
    private final ModelListProperties modelListProperties;

    public DashscopeAPI(ChatModel chatModel, ModelListProperties modelListProperties) {
        // 构造时，可以设置 ChatClient 的参数
        // {@link org.springframework.ai.chat.client.ChatClient};
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build()).build();

        this.modelListProperties = modelListProperties;
    }

    @Override
    public FoodIdentification identifyFood(MultipartFile image) {
        String prompt = """
                    请识别这张图片中的食物，并分析包含的主要食材。
                    注意米饭、面条、米线等需要列为主要食材
                    直接返回JSON格式，不要包含任何其他文本或格式：
                    {
                        "foods": ["食物1", "食物2"],
                        "ingredients": ["食材1", "食材2"]
                    }
                """;
        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_JPEG, image.getResource()));
        UserMessage message = UserMessage.builder().media(mediaList).text(prompt).build();
        message.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);

        Prompt chatPrompt = new Prompt(message, DashScopeChatOptions.builder().withModel(modelListProperties.getVision())  // 使用视觉模型
                .withMultiModel(true)             // 启用多模态
                .withVlHighResolutionImages(true) // 启用高分辨率图片处理
                .withTemperature(0.7).build());
        log.info("identifyFood request: {}", chatPrompt);
        String content = dashScopeChatClient.prompt(chatPrompt).call().content();
        log.info("identifyFood response: {}", content);
        if (content == null) {
            return new FoodIdentification(List.of(), List.of());
        }
        // 移除可能的代码块标记
        content = content.replace("```json", "").replace("```", "").trim();
        JSONObject jsonObject = JSONObject.parseObject(content);
        return new FoodIdentification(jsonObject.getList("foods", String.class), jsonObject.getList("ingredients", String.class));
    }

    @Override
    public NutritionInfo getNutritionInfo(FoodIdentification identification) {
        String prompt = """
                    为以下食物估算营养信息：%s
                    返回JSON格式，包含热量、蛋白质、脂肪、碳水化合物，需要带上单位
                    直接返回JSON格式，不要包含任何其他文本或格式：
                    {
                        "米饭": {
                          "热量": "116 kcal/100g",
                          "蛋白质": "2.6 g/100g",
                          "脂肪": "0.3 g/100g",
                          "碳水化合物": "25.9 g/100g"
                        }
                }""";
        UserMessage message = UserMessage.builder().text(prompt.formatted(identification.toString())).build();

        Prompt chatPrompt = new Prompt(message, DashScopeChatOptions.builder().withModel(modelListProperties.getText())  // 使用视觉模型
                .build());
        log.info("identifyFood request: {}", chatPrompt);
        String content = dashScopeChatClient.prompt(chatPrompt).call().content();
        log.info("getNutritionInfo response: {}", content);
        if (content == null) {
            return new NutritionInfo();
        }
        // 移除可能的代码块标记
        content = content.replace("```json", "").replace("```", "").trim();
        JSONObject jsonObject = JSONObject.parseObject(content);
        return new NutritionInfo();
    }
}
