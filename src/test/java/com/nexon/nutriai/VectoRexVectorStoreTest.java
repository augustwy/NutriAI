package com.nexon.nutriai;

import com.nexon.nutriai.ai.embed.vector_store.VectoRexVectorStore;
import com.nexon.nutriai.service.DocumentEmbeddingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NutriaiApplication.class)
public class VectoRexVectorStoreTest {

    @Autowired
    private VectoRexVectorStore vectorStore;
    @Autowired
    private DocumentEmbeddingService documentEmbeddingService;

    @Test
    public void embed() {
        String fileName = "第一章 营养学基础（知识库版）.docx";
        String filePath = "E:\\xwechat_files\\wxid_arn2at1289ib21_8875\\msg\\file\\2025-11\\第一章 营养学基础（知识库版）.docx";
        String fileType = "WORD";
        documentEmbeddingService.embed(fileName, filePath, fileType);
    }

    @Test
    public void getCollections() {
        vectorStore.getCollections();
    }

    @Test
    public void delCollection() {
        vectorStore.delCollection("nutri");
    }
}
