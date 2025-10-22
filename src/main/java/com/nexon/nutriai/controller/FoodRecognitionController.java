package com.nexon.nutriai.controller;

import com.nexon.nutriai.pojo.response.BaseResponse;
import com.nexon.nutriai.service.FoodRecognitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/web/food")
@RequiredArgsConstructor
public class FoodRecognitionController {

    private final FoodRecognitionService foodRecognitionService;

    @PostMapping("/recognize")
    public BaseResponse<String> recognizeFood(@RequestParam("file") MultipartFile file) {
        String result = foodRecognitionService.recognizeAndAnalyze(file);
        return new BaseResponse<>(result);
    }
}
