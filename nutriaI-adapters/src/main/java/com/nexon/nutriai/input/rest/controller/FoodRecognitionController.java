package com.nexon.nutriai.input.rest.controller;

import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.domain.service.response.FoodIdentificationRes;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.pojo.BaseResponse;
import com.nexon.nutriai.ports.input.service.FoodRecognitionService;
import com.nexon.nutriai.util.WebFluxUtil;
import com.nexon.nutriai.utils.DateUtils;
import com.nexon.nutriai.utils.UUIDUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebExchange;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/web/food")
@RequiredArgsConstructor
public class FoodRecognitionController extends BaseController {

    private final FoodRecognitionService foodRecognitionService;

    /**
     * 食物识别
     *
     * @param file
     * @return
     */
    @GetMapping("/recognize")
    public BaseResponse<FoodIdentificationRes> recognizeFood(@RequestParam("file") MultipartFile file, ServerWebExchange exchange) {
        String chatId = WebFluxUtil.getChatId(exchange);
        String phone = WebFluxUtil.getPhone(exchange);
        String filePath = saveImage(file);
        FoodIdentificationRes result = foodRecognitionService.recognize(filePath, phone, chatId);
        return new BaseResponse<>(result);
    }

    /**
     * 营养报告
     *
     * @param res
     * @return
     */
    @GetMapping("/nutritionReport")
    public BaseResponse<String> nutritionReport(@RequestBody FoodIdentificationRes res, ServerWebExchange exchange) {
        String phone = WebFluxUtil.getPhone(exchange);
        String result = foodRecognitionService.nutritionReport(res.foodIdentification(), res.id(), phone);
        BaseResponse<String> success = new BaseResponse<>(ErrorCode.SUCCESS);
        success.setData(result);
        return success;
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
