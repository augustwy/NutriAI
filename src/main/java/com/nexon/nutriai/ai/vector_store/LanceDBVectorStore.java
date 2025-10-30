package com.nexon.nutriai.ai.vector_store;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LanceDBVectorStore implements VectorStore {
    
    private final ReactiveLanceDBClient lanceDBClient;
    private final String tableName;
    private final EmbeddingModel embeddingModel;
    
    public LanceDBVectorStore(ReactiveLanceDBClient lanceDBClient, EmbeddingModel embeddingModel) {
        this.lanceDBClient = lanceDBClient;
        this.tableName = "documents";
        this.embeddingModel = embeddingModel;
    }
    
    @Override
    public void add(List<Document> documents) {
        List<ReactiveLanceDBClient.DocumentData> documentDataList = documents.stream()
                .map(doc -> {
                    // 从Document中获取嵌入向量和内容
                    List<Double> vector = new ArrayList<>();
                    // 如果文档中已经有嵌入向量，则直接使用
                    Object embeddingObj = doc.getMetadata().get("embedding");
                    if (embeddingObj instanceof List) {
                        vector = ((List<?>) embeddingObj).stream()
                                .map(obj -> {
                                    if (obj instanceof Number) {
                                        return ((Number) obj).doubleValue();
                                    }
                                    return 0.0;
                                })
                                .collect(Collectors.toList());
                    } else if (embeddingModel != null) {
                        // 否则使用嵌入模型生成向量
                        float[] embedding = embeddingModel.embed(doc);
                        List<Double> result = new ArrayList<>(embedding.length);
                        for (float v : embedding) {
                            result.add((double) v);
                        }
                    }
                    
                    return new ReactiveLanceDBClient.DocumentData(
                            doc.getId(),
                            doc.getText(), // 使用getText()方法获取内容
                            vector,
                            doc.getMetadata() // 包含所有元数据
                    );
                })
                .collect(Collectors.toList());
        
        lanceDBClient.addDocuments(tableName, documentDataList)
                .onErrorResume(e -> {
                    System.err.println("Failed to add documents to LanceDB: " + e.getMessage());
                    return Mono.empty();
                })
                .block(); // Blocking for synchronous interface compatibility
    }

    @Override
    public void delete(List<String> idList) {
        lanceDBClient.deleteDocuments(tableName, idList)
                .onErrorResume(e -> {
                    System.err.println("Failed to delete documents from LanceDB: " + e.getMessage());
                    return Mono.empty();
                })
                .block(); // Blocking for synchronous interface compatibility
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        // LanceDB VectorStore implementation does not currently support filter-based deletion
        System.out.println("Filter-based deletion is not supported in current LanceDB implementation");
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        // Convert query text to embedding vector using the embedding model
        List<Double> queryVector = request.getQuery() != null ? 
                convertTextToVector(request.getQuery()) : 
                new ArrayList<>();
        
        int topK = request.getTopK() > 0 ? request.getTopK() : 4;
        double threshold = request.getSimilarityThreshold();
        
        return lanceDBClient.search(tableName, queryVector, topK)
                .filter(result -> result.getScore() >= threshold)
                .map(result -> {
                    Document document = new Document(result.getId(), result.getContent(), result.getMetadata());
                    return document;
                })
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }
    
    /**
     * Convert text to vector using the embedding model.
     */
    private List<Double> convertTextToVector(String text) {
        if (embeddingModel != null) {
            // EmbeddingModel.embed 返回 List<Float>，需要转换为 List<Double>
            float[] embedding = embeddingModel.embed(text);
            List<Double> result = new ArrayList<>(embedding.length);
            for (float v : embedding) {
                result.add((double) v);
            }
            return result;
        }
        // 如果没有嵌入模型，返回空列表
        return new ArrayList<>();
    }
}
