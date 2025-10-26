package com.nexon.nutriai.ports.input.service;

import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.domain.entity.DialogueLog;
import com.nexon.nutriai.domain.service.FoodIdentification;
import com.nexon.nutriai.domain.service.response.FoodIdentificationRes;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.ports.output.ai.TextAPI;
import com.nexon.nutriai.ports.output.ai.VisionAPI;
import com.nexon.nutriai.ports.output.repository.DialogueLogPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class FoodRecognitionService {

    private final VisionAPI visionAPI;
    private final TextAPI textAPI;
    private final DialogueLogPort dialogueLogPort;

    public FoodIdentificationRes recognize(String filePath, String phone, String chatId) {

        DialogueLog dialogueLog = new DialogueLog();
        dialogueLog.setPhone(phone);
        dialogueLog.setRequestId(chatId);
        dialogueLog.setQuestion("file://" + filePath);
        // 记录日志
        DialogueLog save = dialogueLogPort.save(dialogueLog);

        // 调用视觉API识别食物
        FoodIdentification identification = visionAPI.analyzeFoodImage(filePath, phone);
        return new FoodIdentificationRes(identification, save.getId());
    }

    public String nutritionReport(FoodIdentification identification, Long dialogueLogId, String phone) {
        if (identification == null || dialogueLogId == null) {
            throw new NutriaiException(ErrorCode.NONE_PARAM_ERROR, "对话记录不存在");
        }
        Optional<DialogueLog> optional = dialogueLogPort.findById(dialogueLogId);
        if (optional.isEmpty()) {
            throw new NutriaiException(ErrorCode.NONE_PARAM_ERROR, "对话记录不存在");
        }
        DialogueLog dialogueLog = optional.get();
        // 调用文本API分析营养元素
        String analyzeNutrition = textAPI.generateNutritionReport(identification, phone);

        dialogueLog.setAnswer(analyzeNutrition);
        dialogueLogPort.save(dialogueLog);
        return analyzeNutrition;
    }
}
