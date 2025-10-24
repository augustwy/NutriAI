package com.nexon.nutriai.service;

import com.nexon.nutriai.api.TextAPI;
import com.nexon.nutriai.api.VisionAPI;
import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.pojo.FoodIdentification;
import com.nexon.nutriai.pojo.response.FoodIdentificationRes;
import com.nexon.nutriai.repository.DialogueLogRepository;
import com.nexon.nutriai.repository.entity.DialogueLog;
import com.nexon.nutriai.util.DateUtils;
import com.nexon.nutriai.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodRecognitionService {

    private final VisionAPI visionAPI;
    private final TextAPI textAPI;
    private final DialogueLogRepository dialogueLogRepository;

    @Transactional
    public FoodIdentificationRes recognize(MultipartFile image, String phone, String chatId) {

        String filePath = saveImage(image);
        DialogueLog dialogueLog = new DialogueLog();
        dialogueLog.setPhone(phone);
        dialogueLog.setRequestId(chatId);
        dialogueLog.setQuestion("file://" + filePath);
        // 记录日志
        DialogueLog save = dialogueLogRepository.save(dialogueLog);

        // 调用视觉API识别食物
        FoodIdentification identification = visionAPI.analyzeFoodImage(filePath, phone);
        return new FoodIdentificationRes(identification, save.getId());
    }

    @Transactional
    public String nutritionReport(FoodIdentification identification, Long dialogueLogId, String phone) {
        if (identification == null || dialogueLogId == null) {
            throw new NutriaiException(ErrorCode.NONE_PARAM_ERROR, "对话记录不存在");
        }
        Optional<DialogueLog> optional = dialogueLogRepository.findById(dialogueLogId);
        if (optional.isEmpty()) {
            throw new NutriaiException(ErrorCode.NONE_PARAM_ERROR, "对话记录不存在");
        }
        DialogueLog dialogueLog = optional.get();
        // 调用文本API分析营养元素
        String analyzeNutrition = textAPI.generateNutritionReport(identification, phone);

        dialogueLog.setAnswer(analyzeNutrition);
        dialogueLogRepository.save(dialogueLog);
        return analyzeNutrition;
    }

    /**
     * 保存图片到本地存储
     *
     * @param image 图片文件
     * @return 图片保存路径
     */
    private String saveImage(MultipartFile image) {
        try {
            // 获取项目根路径
            String projectRoot = System.getProperty("user.dir");

            // 构建保存路径：项目根路径 + images目录 + 时间戳目录
            String baseDir = projectRoot + "/images/" + DateUtils.today() + "/";
            Path path = Paths.get(baseDir);

            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // 生成安全的唯一文件名，避免使用原始文件名
            String originalFilename = image.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String secureFileName = UUIDUtil.generateUUID() + fileExtension;

            // 完整文件路径
            String filePath = baseDir + secureFileName;
            image.transferTo(new File(filePath));
            log.info("图片保存成功, 文件路径: {}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("图片保存失败", e);
            throw new NutriaiException(ErrorCode.IMAGE_RECOGNITION_ERROR, "图片保存失败");
        }
    }
}
