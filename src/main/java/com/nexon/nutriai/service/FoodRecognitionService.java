package com.nexon.nutriai.service;

import com.nexon.nutriai.api.TextAPI;
import com.nexon.nutriai.api.VisionAPI;
import com.nexon.nutriai.pojo.FoodIdentification;
import com.nexon.nutriai.pojo.FoodRecognitionResponse;
import com.nexon.nutriai.repository.DialogueLogRepository;
import com.nexon.nutriai.repository.entity.DialogueLog;
import com.nexon.nutriai.util.DateUtils;
import com.nexon.nutriai.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FoodRecognitionService {

    private final VisionAPI visionAPI;
    private final TextAPI textAPI;
    private final DialogueLogRepository dialogueLogRepository;

    public FoodRecognitionService(VisionAPI visionAPI, TextAPI textAPI, DialogueLogRepository dialogueLogRepository) {
        this.visionAPI = visionAPI;
        this.textAPI = textAPI;
        this.dialogueLogRepository = dialogueLogRepository;
    }

    @Transactional
    public String recognizeAndAnalyze(MultipartFile image) {
        String phone = ThreadLocalUtil.getPhone();

        String filePath = saveImage(image);
        DialogueLog dialogueLog = new DialogueLog();
        dialogueLog.setPhone(phone);
        dialogueLog.setRequestId(ThreadLocalUtil.getChatId());
        dialogueLog.setQuestion("file://" + filePath);

        // 调用视觉API识别食物
        FoodIdentification identification = visionAPI.analyzeFoodImage(filePath);

        // 调用文本API分析营养元素
        String analyzeNutrition = textAPI.generateNutritionReport(identification);

        dialogueLog.setAnswer(analyzeNutrition);

        // 记录日志
        dialogueLogRepository.save(dialogueLog);
        return analyzeNutrition;
    }

    /**
     * 保存图片到本地存储
     *
     * @param image 图片文件
     * @return 图片保存路径
     */
    public String saveImage(MultipartFile image) {
        try {
            // 获取项目根路径
            String projectRoot = System.getProperty("user.dir");

            // 构建保存路径：项目根路径 + images目录 + 时间戳目录
            String baseDir = projectRoot + "/images/" + DateUtils.today() + "/";
            Path path = Paths.get(baseDir);

            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // 完整文件路径
            String filePath = baseDir + image.getOriginalFilename();
            image.transferTo(new File(filePath));
            log.info("图片保存成功, 文件路径: {}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("图片保存失败", e);
            return null;
        }
    }
}
