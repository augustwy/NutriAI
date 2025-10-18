package com.nexon.nutriai.controller;

import com.nexon.nutriai.pojo.FoodRecognitionResponse;
import com.nexon.nutriai.service.FoodRecognitionService;
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

    @Autowired
    private FoodRecognitionService foodRecognitionService;

    @PostMapping("/recognize")
    public ResponseEntity<FoodRecognitionResponse> recognizeFood(@RequestParam("file") MultipartFile file) {
        FoodRecognitionResponse response = foodRecognitionService.recognizeAndAnalyze(file);
        return ResponseEntity.ok(response);
    }
}
