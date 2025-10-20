package com.nexon.nutriai.api.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.api.TextAPI;
import com.nexon.nutriai.api.VisionAPI;
import com.nexon.nutriai.config.ModelListProperties;
import com.nexon.nutriai.constant.ErrorCode;
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

    /**
     * 识别图片中的食物，并返回识别结果
     *
     * @param image 图片文件
     * @return 食物识别结果
     */
    @Override
    public FoodIdentification analyzeFoodImage(MultipartFile image) {
        String prompt = """
                请仔细分析这张图片中的食物，并提供准确的识别结果。
                
                要求：
                1. 将可明确识别的食物列在foods数组中（如：米饭、面条、炒饭等），并估算食物的重量（单位：克，精度保留小数点后两位），如果无法估算则返回0
                2. 将组成食物的主要食材列在ingredients数组中（如：大米、鸡蛋、青菜等），并估算其在食物中的占比（%），如果无法估算则返回0
                3. 米饭、面条、米线等主食必须列在ingredients中
                4. 识别图片中食物的烹饪方式（cookingMethod），如：煎、炸、炒、煮、蒸、凉拌等
                5. 如果图片中没有包含任何食物，就返回空字符串
                6. 只返回有效的JSON格式，不包含任何其他文本：
                {
                    "foods": [
                        {
                            name: "食物1",
                            cookingMethod: "炒",
                            weight: 100.00
                        },
                        {
                            name: "食物2",
                            cookingMethod: "凉拌",
                            weight: 100.00
                        }
                    ],
                    "ingredients": [
                        {
                            name: "食材1",
                            food: "食物1",
                            proportion: 100
                        },
                        {
                            name: "食材2",
                            food: "食物2",
                            proportion: 50
                        },
                        {
                            name: "食材3",
                            food: "食物2",
                            proportion: 10
                        }
                    ]
                }
                """;
        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_JPEG, image.getResource()));
        UserMessage message = UserMessage.builder().media(mediaList).text(prompt).build();
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
        String prompt = """
                基于以下食物信息，分析这些食物的营养成分并提供详细的营养分析报告：
                %s
                
                要求：
                1. 为列表中的每一种食物分别提供营养信息，包含四大核心营养素：热量、蛋白质、脂肪、碳水化合物
                2. 所有数值均基于食物的实际重量计算
                3. 提供整体饮食的营养评估（仅基于提供的食物）
                4. 根据营养分析给出针对这些食物的具体饮食建议
                5. 指出这些食物在营养过剩或不足的方面
                6. 严格按照以下Markdown格式输出，不包含任何额外文本：
                
                   # 营养分析报告
                
                   ## 食物详情
                
                   | 食物名称 | 重量(克) | 热量(千卡) | 蛋白质(克) | 脂肪(克) | 碳水化合物(克) | 营养分析 |
                   |---------|---------|-----------|-----------|---------|---------------|---------|
                   | {食物名称1} | {重量1} | {热量1} | {蛋白质1} | {脂肪1} | {碳水化合物1} | {该食物的营养分析} |
                   | {食物名称2} | {重量2} | {热量2} | {蛋白质2} | {脂肪2} | {碳水化合物2} | {该食物的营养分析} |
                
                   ## 总体分析
                
                   - **总热量**: {总热量数值} 千卡
                   - **总蛋白质**: {总蛋白质数值} 克
                   - **总脂肪**: {总脂肪数值} 克
                   - **总碳水化合物**: {总碳水化合物数值} 克
                   - **营养评估**: {对整体饮食的营养评估，仅基于提供的食物，包括热量是否适宜、营养是否均衡等}
                
                   ## 饮食建议
                
                   1. {针对这些食物的具体饮食建议1，如某种食物摄入量的调整}
                   2. {针对这些食物的具体饮食建议2，如食物搭配的优化}
                   3. {针对这些食物的具体饮食建议3，如烹饪方式的建议}
                
                   ---
                
                   > 本建议由AI生成，食物制作过程中存在差异，结果仅供参考。
                """;


        UserMessage message = UserMessage.builder()
                .text(prompt.formatted(buildFoodDescription(identification)))
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
