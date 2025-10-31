package com.nexon.nutriai.service;

import com.nexon.nutriai.ai.EmbedAPI;
import com.nexon.nutriai.pojo.request.AiEmbedRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentEmbeddingService {

    private final EmbedAPI embedAPI;

    public void embed(String content) {
        AiEmbedRequest aiEmbedRequest = new AiEmbedRequest();
        embedAPI.embed(aiEmbedRequest);
    }

    public void query(String question) {
        embedAPI.query(question);
    }
}
