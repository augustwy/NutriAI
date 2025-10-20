package com.nexon.nutriai.api.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.api.TextAPI;
import com.nexon.nutriai.api.VisionAPI;
import com.nexon.nutriai.config.DashscopeModelProperties;
import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.constant.PromptConstant;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.pojo.FoodIdentification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private final DashscopeModelProperties modelListProperties;

    public DashscopeAPI(ChatModel chatModel, DashscopeModelProperties modelListProperties) {
        // 构造时，可以设置 ChatClient 的参数
        // {@link org.springframework.ai.chat.client.ChatClient};
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build()).build();

        this.modelListProperties = modelListProperties;
    }

    /**
     * 识别图片中的食物，并返回识别结果
     *
     * @param image 图片文件
     * @return 食物识别结果
     */
    @Override
    public FoodIdentification analyzeFoodImage(MultipartFile image) {
        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_JPEG, image.getResource()));
        UserMessage message = UserMessage.builder().media(mediaList).text(PromptConstant.IMAGE_IDENTIFY_PROMPT).build();
        message.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);

        Prompt chatPrompt = new Prompt(message, DashScopeChatOptions.builder().withModel(modelListProperties.getVision())  // 使用视觉模型
                .withMultiModel(true)             // 启用多模态
                .withVlHighResolutionImages(true) // 启用高分辨率图片处理
                .withTemperature(0.7).build());
        log.debug("identifyFood request: {}", chatPrompt);
        String content = dashScopeChatClient.prompt(chatPrompt).call().content();
        log.info("identifyFood response: {}", content);
        if (StringUtils.isEmpty(content)) {
            throw new NutriaiException(ErrorCode.IMAGE_RECOGNITION_ERROR, "无法识别图片中的食物");
        }
        // 移除可能的代码块标记
        content = content.replace("```json", "").replace("```", "").trim();
        return JSONObject.parseObject(content, FoodIdentification.class);
    }

    @Override
    public String generateNutritionReport(FoodIdentification identification) {
        log.info("generateNutritionReport request: {}", identification);
        if (identification == null || identification.getFoods() == null || identification.getFoods().isEmpty()) {
            throw new NutriaiException(ErrorCode.IMAGE_RECOGNITION_ERROR, "无法识别图片中的食物");
        }

        UserMessage message = UserMessage.builder()
                .text(PromptConstant.NUTRITION_ANALYZE_REPORT_PROMPT.formatted(buildFoodDescription(identification)))
                .build();

        Prompt chatPrompt = new Prompt(message, DashScopeChatOptions.builder()
                .withModel(modelListProperties.getText())
                .build());

        log.debug("generateNutritionReport request: {}", chatPrompt);
        String content = dashScopeChatClient.prompt(chatPrompt).call().content();
        log.info("generateNutritionReport response: {}", content);
        return content;
    }

    private static String buildFoodDescription(FoodIdentification identification) {
        StringBuilder foods = new StringBuilder();
        for (FoodIdentification.Food food : identification.getFoods()) {
            foods.append(food.getName())
                    .append(" ")
                    .append(food.getWeight())
                    .append("克 ")
                    .append("烹饪方式：")
                    .append(food.getCookingMethod())
                    .append("; ");
        }

        StringBuilder ingredients = new StringBuilder();
        for (FoodIdentification.Ingredient ingredient : identification.getIngredients()) {
            // 对比例字段进行处理，移除或转义特殊字符
            String cleanProportion = ingredient.getProportion().replace("%", "%%");
            ingredients.append("食材名: ")
                    .append(ingredient.getName())
                    .append(", 所属食物: ")
                    .append(ingredient.getFood())
                    .append(", 占比: ")
                    .append(cleanProportion)
                    .append("; ");
        }

        return "食物：" + foods + "\n食材：" + ingredients;
    }
}
