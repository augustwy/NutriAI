package com.nexon.nutriai.controller;

import com.nexon.nutriai.pojo.response.BaseResponse;
import com.nexon.nutriai.service.DocumentEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DocumentEmbeddingController {

    private final DocumentEmbeddingService documentEmbeddingService;

    @PostMapping("/embed")
    public BaseResponse<Void> embed() {
        String fileName = "第一章 营养学基础（知识库版）.docx";
        String filePath = "E:\\xwechat_files\\wxid_arn2at1289ib21_8875\\msg\\file\\2025-11\\第一章 营养学基础（知识库版）.docx";
        String fileType = "WORD";
        documentEmbeddingService.embed(fileName, filePath, fileType);

        return BaseResponse.success();
    }

    @GetMapping("/query")
    public BaseResponse<Void> query(String query) {
        documentEmbeddingService.query(query);

        return BaseResponse.success();
    }
}
