package com.nexon.nutriai.api.impl;

import com.alibaba.fastjson2.JSONObject;
import com.nexon.nutriai.api.TextAPI;
import com.nexon.nutriai.api.VisionAPI;
import com.nexon.nutriai.pojo.FoodIdentification;
import com.nexon.nutriai.pojo.NutritionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@Slf4j
public class DashscopeAPI implements VisionAPI, TextAPI {

    private final ChatModel dashScopeChatModel;
    public DashscopeAPI(@Qualifier("dashscopeChatModel")ChatModel dashScopeChatModel) {
        this.dashScopeChatModel = dashScopeChatModel;
    }

    @Override
    public FoodIdentification identifyFood(MultipartFile image) {
        log.info("DashscopeAPI: identifyFood: image: {}", image);
        UserMessage userMessage = UserMessage.builder()
                .media(new Media(new MimeType("image/png"), image.getResource()))
                .text("请识别图片中的食物名称和数量，并返回一个JSON格式的列表，每个元素包含食物名称和数量。")
                .build();

        ChatOptions runtimeOptions = ChatOptions.builder().model("qwen-vl-plus").build();
        Generation gen = dashScopeChatModel.call(new Prompt(userMessage.toString(), runtimeOptions)).getResult();
        String text = gen.getOutput().getText();
        JSONObject jsonObject = JSONObject.parseObject(text);
        return new FoodIdentification(jsonObject.getList("foods", String.class).toArray());
    }

    @Override
    public NutritionInfo getNutritionInfo(List<String> foods) {
        return null;
    }
}
