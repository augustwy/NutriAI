package com.nexon.nutriai.service;

import com.nexon.nutriai.ai.EmbedAPI;
import com.nexon.nutriai.ai.embed.Knowledge;
import com.nexon.nutriai.pojo.request.AiEmbedRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentEmbeddingService {

    private final EmbedAPI embedAPI;

    public void embed(String fileName, String filePath, String fileType) {
        AiEmbedRequest aiEmbedRequest = new AiEmbedRequest(List.of(new Knowledge(fileName, filePath, Knowledge.FileType.getFileType(fileType))));
        embedAPI.embed(aiEmbedRequest);
    }

    public void query(String question) {
        embedAPI.query(question);
    }
}
