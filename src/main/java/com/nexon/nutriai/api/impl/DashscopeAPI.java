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
                2. 将组成食物的主要食材列在ingredients数组中（如：大米、鸡蛋、青菜等），并估算其在食物中的占比，如果无法估算则返回0
                3. 米饭、面条、米线等主食必须列在ingredients中
                4. 只返回有效的JSON格式，不包含任何其他文本：
                {
                    "foods": [
                        {
                            name: "食物1",
                            weight: 100.00
                        },
                        {
                            name: "食物2",
                            weight: 100.00
                        }
                    ],
                    "ingredients": [
                        {
                            name: "食材1",
                            food: "食物1",
                            proportion: 100%
                        },
                        {
                            name: "食材2",
                            food: "食物2",
                            proportion: 50%
                        },
                        {
                            name: "食材3",
                            food: "食物2",
                            proportion: 10%
                        }
                    ]
                }
                5. 如果图片中没有包含任何食物，就返回空字符串
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
            return null;
        }
        // 移除可能的代码块标记
        content = content.replace("```json", "").replace("```", "").trim();
        return JSONObject.parseObject(content, FoodIdentification.class);
    }

    /**
     * 计算食物的营养信息
     *
     * @param identification 食物识别结果
     * @return 营养信息
     */
    @Override
    public NutritionInfo calculateNutrition(FoodIdentification identification) {
        log.info("calculateNutrition request: {}", identification);
        String prompt = """
                基于以下食物信息，为每种食物提供详细的营养成分数据：
                %s
                
                要求：
                1. 为列表中的每一种食物分别提供营养信息
                2. 根据食材在食物中的占比，计算所有食物的总热量(单位：kcal)
                2. 包含四大核心营养素：热量(calorie)、蛋白质(protein)、脂肪(fat)、碳水化合物(carbohydrate)
                3. 所有数值均以100克为基准单位
                4. 严格按照以下JSON格式输出，不包含任何额外文本：
                {
                  "total_calorie": xx.x,
                  "ingredients": [
                    {
                        "name": "食物名称1",
                        "calorie": "xxx kcal/100g",
                        "protein": "x.x g/100g",
                        "fat": "x.x g/100g",
                        "carbohydrate": "xx.x g/100g"
                    },
                    {
                      "name": "食物名称2"
                      "calorie": "xxx kcal/100g",
                      "protein": "x.x g/100g",
                      "fat": "x.x g/100g",
                      "carbohydrate": "xx.x g/100g"
                    }
                  ]
                }
                """;
        UserMessage message = UserMessage.builder().text(prompt.formatted(buildFoodDescription(identification))).build();

        Prompt chatPrompt = new Prompt(message, DashScopeChatOptions.builder().withModel(modelListProperties.getText())
                .build());
        log.debug("identifyFood request: {}", chatPrompt);
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

    private static String buildFoodDescription(FoodIdentification identification) {
        StringBuilder foods = new StringBuilder();
        for (FoodIdentification.Food food : identification.getFoods()) {
            foods.append(food.getName()).append(food.getWeight()).append("克");
        }

        StringBuilder ingredients = new StringBuilder();
        for (FoodIdentification.Ingredient ingredient : identification.getIngredients()) {
            ingredients.append(ingredient.toString()).append(";");
        }

        return "食物：" + foods + "\n食材：" + ingredients;
    }
}
