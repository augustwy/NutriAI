package com.nexon.nutriai.ai.model.dashscope;

import com.nexon.nutriai.ai.EmbedAPI;
import com.nexon.nutriai.config.properties.ModelProperties;
import com.nexon.nutriai.pojo.request.AiEmbedRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.models.providers.dashscope.embed")
public class DashscopeEmbed implements EmbedAPI {

    private final String model;
    private final ObjectProvider<VectorStore> vectorStoreProvider;

    public DashscopeEmbed(ModelProperties modelProperties,  ObjectProvider<VectorStore> vectorStoreProvider) {
        this.model = modelProperties.providers().get("dashscope").embed();
        this.vectorStoreProvider = vectorStoreProvider;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public List<String> embed(AiEmbedRequest aiEmbedRequest) {
        // 在实际使用时才获取VectorStore实例
        VectorStore vectorStore = vectorStoreProvider.getObject();

        // 1. 解析文件
        String content = "这是一个测试文档，用于验证向量数据库功能是否正常工作。";

        // 2. 分割文件
        List<String> chunks = Arrays.asList(
                "这是测试文档的第一部分。",
                "这是测试文档的第二部分。",
                "这是测试文档的第三部分。"
        );

        // 3. 获取嵌入向量
        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(chunk, Map.of("source", "test")))
                .toList();

        // 4. 保存向量
        try {
            vectorStore.add(documents);
            log.info("成功向向量数据库添加了 {} 个文档片段", documents.size());

            // 进行相似性搜索测试
            List<Document> similarDocs = vectorStore.similaritySearch("测试文档");
            log.info("相似性搜索返回了 {} 个结果", similarDocs.size());

            return chunks;
        } catch (Exception e) {
            log.error("向量数据库操作失败", e);
            return List.of("向量数据库操作失败: " + e.getMessage());
        }
    }
}
