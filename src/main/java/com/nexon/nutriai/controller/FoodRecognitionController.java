package com.nexon.nutriai.controller;

import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.constant.HttpHeaderConstant;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.pojo.request.BaseRequest;
import com.nexon.nutriai.pojo.response.BaseResponse;
import com.nexon.nutriai.pojo.response.FoodIdentificationRes;
import com.nexon.nutriai.service.FoodRecognitionService;
import com.nexon.nutriai.util.DateUtils;
import com.nexon.nutriai.util.UUIDUtil;
import com.nexon.nutriai.util.WebFluxUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/web/food")
@RequiredArgsConstructor
public class FoodRecognitionController extends BaseController {

    private final FoodRecognitionService foodRecognitionService;

    /**
     * 食物识别
     *
     * @param filePart
     * @return
     */
    @PostMapping("/recognize")
    public Mono<BaseResponse<FoodIdentificationRes>> recognizeFood(
            @RequestPart("file") FilePart filePart,
            ServerWebExchange exchange) {

        // 1. 准备目标文件的绝对路径
        String destinationPath = saveFilePart(filePart); // 这个方法现在只生成路径
        Path destination = Paths.get(destinationPath);

        BaseRequest request = new BaseRequest();
        request.setPhone(WebFluxUtil.getPhone(exchange));
        request.setChatId(WebFluxUtil.getChatId(exchange));

        // 2. 创建响应式处理链
        return filePart.transferTo(destination.toFile()) // 步骤A: 异步保存文件，返回 Mono<Void>
                .then(Mono.fromCallable(() -> {         // 步骤B: 当A完成后，执行这个Callable
                    // 这里是阻塞操作，会在 boundedElastic 线程池中执行
                    log.info("图片保存成功, 文件路径: {}", destinationPath);
                    FoodIdentificationRes result = foodRecognitionService.recognize(destinationPath, request);
                    return new BaseResponse<>(result);
                }))
                .subscribeOn(Schedulers.boundedElastic()); // 将整个链中的阻塞操作调度到弹性线程池
    }

    /**
     * 营养报告
     *
     * @param res
     * @return
     */
    @PostMapping(value = "/nutritionReport", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> nutritionReport(@RequestBody FoodIdentificationRes res, ServerWebExchange exchange) {
        String phone = WebFluxUtil.getPhone(exchange);
        BaseRequest request = new BaseRequest();
        request.setPhone(phone);

        String chatId = phone + "-NR-" + UUIDUtil.generateShortUUID(16);
        exchange.getResponse().getHeaders().set(HttpHeaderConstant.RESPONSE_HEADER_CHAT_ID, chatId);
        exchange.getResponse().getHeaders().set(HttpHeaderConstant.RESPONSE_HEADER_MODEL, foodRecognitionService.getTextModel());

        return foodRecognitionService.nutritionReport(res.foodIdentification(), request);
    }

    private String saveFilePart(FilePart filePart) {
        try {
            // 获取项目根路径
            String projectRoot = System.getProperty("user.dir");
            // 构建保存路径：项目根路径 + images目录 + 时间戳目录
            String uploadDir = projectRoot + "/images/" + DateUtils.today() + "/";

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = filePart.filename();
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + extension;

            Path destination = uploadPath.resolve(newFileName);

            // 将文件内容保存到目标路径
            filePart.transferTo(destination.toFile());

            return destination.toString();
        } catch (IOException e) {
            throw new NutriaiException(ErrorCode.IMAGE_RECOGNITION_ERROR, "图片保存失败");
        }
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
            Path uploadPath = Paths.get(baseDir).toAbsolutePath(); // 关键改动：使用绝对路径
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成安全的唯一文件名，避免使用原始文件名
            String originalFilename = image.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String secureFileName = UUIDUtil.generateUUID() + fileExtension;
            Path destination = uploadPath.resolve(secureFileName);
            // 完整文件路径
            return destination.toString();
        } catch (Exception e) {
            log.error("图片保存失败", e);
            throw new NutriaiException(ErrorCode.IMAGE_RECOGNITION_ERROR, "图片保存失败");
        }
    }
}
