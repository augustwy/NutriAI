package com.nexon.nutriai.controller;

import com.nexon.nutriai.pojo.FoodRecognitionResponse;
import com.nexon.nutriai.pojo.RecipeResponse;
import com.nexon.nutriai.service.FoodRecognitionService;
import com.nexon.nutriai.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/food")
public class FoodRecognitionController {

    private final FoodRecognitionService foodRecognitionService;

    public FoodRecognitionController(FoodRecognitionService foodRecognitionService) {
        this.foodRecognitionService = foodRecognitionService;
    }

    @PostMapping("/recognize")
    public ResponseEntity<FoodRecognitionResponse> recognizeFood(@RequestParam("file") MultipartFile file, @RequestParam("phone")String phone) {
        ThreadLocalUtil.THREAD_LOCAL_PHONE.set(phone);
        String result = foodRecognitionService.recognizeAndAnalyze(file);
        return ResponseEntity.ok(new FoodRecognitionResponse(result));
    }

    @PostMapping("/recipe")
    public ResponseEntity<RecipeResponse> recipe() {
        return ResponseEntity.ok(new RecipeResponse(""));
    }
}
