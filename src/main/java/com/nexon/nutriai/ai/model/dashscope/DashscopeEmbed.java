package com.nexon.nutriai.ai.model.dashscope;

import com.nexon.nutriai.ai.EmbedAPI;
import com.nexon.nutriai.ai.embed.DocumentReader;
import com.nexon.nutriai.ai.embed.Knowledge;
import com.nexon.nutriai.ai.embed.TextSplitter;
import com.nexon.nutriai.ai.embed.vector_store.VectoRexVectorStore;
import com.nexon.nutriai.config.properties.ModelOption;
import com.nexon.nutriai.exception.NutriaiException;
import com.nexon.nutriai.pojo.request.AiEmbedRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Spliterator;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.models.providers.dashscope.embed")
public class DashscopeEmbed implements EmbedAPI {

    private final String model;
    private final VectorStore vectorStore;

    public DashscopeEmbed(ModelOption modelOption, VectoRexVectorStore vectoRexVectorStore) {
        this.model = modelOption.providers().get("dashscope").embed();
        this.vectorStore = vectoRexVectorStore;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public void embed(AiEmbedRequest aiEmbedRequest) {
        if (aiEmbedRequest.getKnowledges() == null || aiEmbedRequest.getKnowledges().isEmpty()) {
            throw new NutriaiException("请提供要嵌入的文档");
        }

        for (Knowledge knowledge : aiEmbedRequest.getKnowledges()) {
            log.info("正在处理文档: {}", knowledge.getFileName());
            try {
                DocumentReader documentReader = new DocumentReader(knowledge);
                Spliterator<String> spliterator = new TextSplitter(TextSplitter.SEMANTIC_BASED, 1000).split(documentReader.read());
                spliterator.forEachRemaining((String line) -> {
                    log.info("正在处理行: {}", line.length() > 50 ? line.substring(0, 50) + "..." : line);
                    // 处理每一行数据
                    Document document = new Document(line, Map.of("name", knowledge.getFileName()));
                    vectorStore.add(List.of(document));
                });
            } catch (Exception e) {
                log.error("处理文档时出错: ", e);
                throw new NutriaiException("处理文档时出错: " + e.getMessage());
            } finally {
                log.info("处理文档完成: {}", knowledge.getFileName());
            }
        }
    }

    @Override
    public List<Document> query(String question) {
        log.info("进行相似性搜索，问题为: {}", question);
        List<Document> documents = vectorStore.similaritySearch(question);
        for (Document similaritySearch : documents) {
            log.info("相似文档: {}", similaritySearch.getText());
        }
        return documents;
    }
}
