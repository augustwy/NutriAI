package com.nexon.nutriai.controller;

import com.nexon.nutriai.pojo.response.BaseResponse;
import com.nexon.nutriai.service.DocumentEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/admin/")
@RequiredArgsConstructor
public class DocumentEmbeddingController {

    private final DocumentEmbeddingService documentEmbeddingService;

    @PostMapping("/embed")
    public BaseResponse<Void> embed() {
        documentEmbeddingService.embed("");

        return BaseResponse.success();
    }
}
