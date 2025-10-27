package com.nexon.nutriai.service;

import com.nexon.nutriai.ai.TextAPI;
import com.nexon.nutriai.ai.VisionAPI;
import com.nexon.nutriai.ai.common.AiTool;
import com.nexon.nutriai.ai.common.AiToolUtil;
import com.nexon.nutriai.constant.PromptConstant;
import com.nexon.nutriai.constant.annotaion.LogAnnotation;
import com.nexon.nutriai.constant.annotaion.TrackSubscription;
import com.nexon.nutriai.pojo.FoodIdentification;
import com.nexon.nutriai.pojo.request.AiVisionRequest;
import com.nexon.nutriai.pojo.request.BaseAiRequest;
import com.nexon.nutriai.pojo.request.BaseRequest;
import com.nexon.nutriai.pojo.response.FoodIdentificationRes;
import com.nexon.nutriai.dao.repository.EatingLogRepository;
import com.nexon.nutriai.dao.entity.EatingLog;
import com.nexon.nutriai.tools.UserTools;
import com.nexon.nutriai.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 食物识别服务类
 * 
 * 该服务负责处理食物图片识别和营养分析功能。
 * 它集成了视觉识别API和文本分析API，提供完整的食物识别和营养分析服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodRecognitionService {

    private final VisionAPI visionAPI;
    private final TextAPI textAPI;
    private final EatingLogRepository eatingLogRepository;
    private final UserTools userTools;

    /**
     * 识别食物图片并保存识别记录
     * 
     * 该方法使用视觉API分析上传的食物图片，识别其中的食物及其相关信息。
     * 识别结果会被保存到饮食日志中。
     * 
     * @param filePath 图片文件路径
     * @param request 基础请求对象，包含用户信息
     * @return 食物识别结果
     */
    @Transactional
    @LogAnnotation(value = "recognize", requestType = LogAnnotation.RequestType.NORMAL)
    public FoodIdentificationRes recognize(String filePath, BaseRequest request) {
        AiVisionRequest aiVisionRequest = new AiVisionRequest(request);
        aiVisionRequest.add(filePath);
        aiVisionRequest.setContent(PromptConstant.IMAGE_IDENTIFY_USER_PROMPT);
        aiVisionRequest.setSystemPrompt(PromptConstant.IMAGE_IDENTIFY_SYSTEM_PROMPT);

        // 调用视觉API识别食物
        FoodIdentification identification = visionAPI.imageAnalyze(aiVisionRequest, FoodIdentification.class);
        if (identification != null) {
            EatingLog eatingLog = new EatingLog();
            eatingLog.setPhone(request.getPhone());
            eatingLog.setFood(identification.toString());

            eatingLogRepository.save(eatingLog);
        }
        return new FoodIdentificationRes(identification);
    }

    /**
     * 生成营养分析报告
     * 
     * 该方法基于食物识别结果，调用文本API生成详细的营养分析报告。
     * 报告以流式方式返回，支持实时显示。
     * 
     * @param identification 食物识别结果
     * @param request 基础请求对象，包含用户信息
     * @return 流式的营养分析报告内容
     */
    @TrackSubscription(value = "nutritionReport", streamIdParamName = "chatId")
    @LogAnnotation(value = "recognize", requestType = LogAnnotation.RequestType.NORMAL)
    public Flux<String> nutritionReport(FoodIdentification identification, BaseRequest request) {
        // 获取模板参数
        Map<String, Object> templateParams = identification.toTemplateParameters();
        templateParams.put("phone", request.getPhone());
        templateParams.put("time", DateUtils.format(LocalTime.now()));

        BaseAiRequest baseAiRequest = new BaseAiRequest(request);
        baseAiRequest.setContent(PromptConstant.NUTRITION_ANALYZE_REPORT_USER_PROMPT);
        baseAiRequest.setSystemPrompt(PromptConstant.NUTRITION_ANALYZE_REPORT_SYSTEM_PROMPT);
        baseAiRequest.setContext(templateParams);

        AiTool userAiTool = AiToolUtil.build(userTools, "用户服务工具类", "userTools");

        // 调用文本API分析营养元素

        return textAPI.textAnalysis4Stream(baseAiRequest, List.of(userAiTool));
    }

    /**
     * 获取文本模型名称
     * 
     * @return 当前使用的文本模型名称
     */
    public String getTextModel() {
        return textAPI.getModel();
    }
}