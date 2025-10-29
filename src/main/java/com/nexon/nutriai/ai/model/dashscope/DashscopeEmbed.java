package com.nexon.nutriai.ai.model.dashscope;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeEmbeddingProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import com.nexon.nutriai.ai.EmbedAPI;
import com.nexon.nutriai.config.properties.ModelProperties;
import com.nexon.nutriai.pojo.request.AiEmbedRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.models.providers.dashscope.embed")
public class DashscopeEmbed implements EmbedAPI {

    private final String model;
    private final VectorStore vectorStore;

    public DashscopeEmbed(ModelProperties modelProperties, ChromaApi chromaApi, DashScopeEmbeddingProperties properties) {
        this.model = modelProperties.providers().get("dashscope").embed();

        EmbeddingModel embeddingModel = new DashScopeEmbeddingModel(DashScopeApi.builder().apiKey(properties.getApiKey()).build(), MetadataMode.EMBED, DashScopeEmbeddingOptions.builder().withModel(model).build());

//        this.vectorStore = ChromaVectorStore.builder(chromaApi, embeddingModel).build();

        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    }

    @Override
    public List<String> embed(AiEmbedRequest aiEmbedRequest) {
        // 1. 解析文件


        // 2. 分割文件

        // 3. 获取嵌入向量

        // 4. 保存向量

        return null;
    }
}
