package com.nexon.nutriai.controller;

import com.nexon.nutriai.constant.ErrorCode;
import com.nexon.nutriai.pojo.response.BaseResponse;
import com.nexon.nutriai.pojo.response.FoodIdentificationRes;
import com.nexon.nutriai.service.FoodRecognitionService;
import com.nexon.nutriai.util.WebFluxUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebExchange;

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
        FoodIdentificationRes result = foodRecognitionService.recognize(file, phone, chatId);
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
}
