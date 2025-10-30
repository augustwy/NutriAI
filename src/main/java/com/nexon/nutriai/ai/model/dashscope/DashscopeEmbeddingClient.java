package com.nexon.nutriai.ai.model.dashscope;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingClient;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DashscopeEmbeddingClient extends AbstractEmbeddingClient {

    private final DashScopeEmbeddingModel dashScopeEmbeddingModel;

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return dashScopeEmbeddingModel.call(request);
    }

    @Override
    public List<Double> embed(Document document) {
        float[] embed = dashScopeEmbeddingModel.embed(document);
        List<Double> list = new ArrayList<>();
        for (float f : embed) {
            list.add((double) f);
        }
        return list;
    }
}
