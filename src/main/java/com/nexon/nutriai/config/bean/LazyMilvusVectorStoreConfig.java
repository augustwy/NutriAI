package com.nexon.nutriai.config.bean;

import io.milvus.client.MilvusServiceClient;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.MilvusVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConditionalOnProperty(name = "spring.ai.vectorstore.milvus.enabled", havingValue = "true", matchIfMissing = true)
public class LazyMilvusVectorStoreConfig {

    @Bean
    @Lazy // 关键：让 MilvusVectorStore 延迟初始化
    public MilvusVectorStore milvusVectorStore(MilvusServiceClient milvusClient, EmbeddingClient embeddingClient) {
        // Spring AI 的 MilvusVectorStore 构造函数
        return new MilvusVectorStore(milvusClient, embeddingClient);
    }
}