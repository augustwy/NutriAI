package com.nexon.nutriai.output.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.constant.PromptConstant;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.ports.output.ai.TextAPI;
import com.nexon.nutriai.ports.output.ai.VisionAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DashscopeAPI implements VisionAPI, TextAPI {

    private final ChatClient dashScopeChatClient;
    private final DashscopeModelProperties modelListProperties;
    private final EatingLogRepository eatingLogRepository;
    private final UserTools userTools;

    public DashscopeAPI(ChatModel chatModel, DashscopeModelProperties modelListProperties, EatingLogRepository eatingLogRepository, UserTools userTools) {
        // 构造时，可以设置 ChatClient 的参数
        // {@link org.springframework.ai.chat.client.ChatClient};
        this.dashScopeChatClient = ChatClient.builder(chatModel)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build()).build();

        this.modelListProperties = modelListProperties;
        this.eatingLogRepository = eatingLogRepository;
        this.userTools = userTools;
    }

    /**
     * 识别图片中的食物，并返回识别结果
     *
     * @param filePath 图片文件地址
     * @return 食物识别结果
     */
    @Override
    public FoodIdentification analyzeFoodImage(String filePath, String phone) {
        log.info("identifyFood request: {}", filePath);

        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_JPEG, new FileSystemResource(filePath)));
        UserMessage userMessage = UserMessage.builder().media(mediaList).text(PromptConstant.IMAGE_IDENTIFY_USER_PROMPT).build();
        userMessage.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);

        SystemMessage systemMessage = new SystemMessage(PromptConstant.IMAGE_IDENTIFY_SYSTEM_PROMPT);
        Prompt chatPrompt = new Prompt(List.of(systemMessage, userMessage),
                DashScopeChatOptions.builder().withModel(modelListProperties.getVision())  // 使用视觉模型
                .withMultiModel(true)             // 启用多模态
                .withVlHighResolutionImages(true) // 启用高分辨率图片处理
                .withTemperature(0.7).build());
        log.debug("identifyFood request: {}", chatPrompt);
        FoodIdentification foodIdentification = dashScopeChatClient.prompt(chatPrompt).call().entity(FoodIdentification.class);
        log.info("identifyFood response: {}", JSONObject.toJSONString(foodIdentification));

        if (foodIdentification != null) {
            EatingLog eatingLog = new EatingLog();
            eatingLog.setPhone(phone);
            eatingLog.setFood(foodIdentification.toString());

            eatingLogRepository.save(eatingLog);
        }
        return foodIdentification;
    }

    @Override
    public String generateNutritionReport(FoodIdentification identification, String phone) {
        log.info("generateNutritionReport request: {}", identification);
        if (identification == null || identification.getFoods() == null || identification.getFoods().isEmpty()) {
            throw new NutriaiException(ErrorCode.IMAGE_RECOGNITION_ERROR, "无法识别图片中的食物");
        }

        PromptTemplate NUTRITION_ANALYZE_REPORT_USER_PROMPT_TEMPLATE = PromptTemplate.builder()
                .template(PromptConstant.NUTRITION_ANALYZE_REPORT_USER_PROMPT)
                .build();

        // 获取模板参数
        Map<String, Object> templateParams = identification.toTemplateParameters();
        templateParams.put("phone", phone);
        templateParams.put("time", DateUtils.format(LocalTime.now()));
        // 渲染模板
        UserMessage userMessage = UserMessage.builder()
                .text(NUTRITION_ANALYZE_REPORT_USER_PROMPT_TEMPLATE.render(templateParams))
                .build();
        SystemMessage systemMessage = new SystemMessage(PromptConstant.NUTRITION_ANALYZE_REPORT_SYSTEM_PROMPT);

        Prompt chatPrompt = new Prompt(List.of(systemMessage, userMessage), DashScopeChatOptions.builder()
                .withModel(modelListProperties.getText())
                .build());

        log.debug("generateNutritionReport request: {}", chatPrompt);
        String content = dashScopeChatClient.prompt(chatPrompt).tools(userTools).call().content();
        log.info("generateNutritionReport response: {}", content);
        return content;
    }
}
