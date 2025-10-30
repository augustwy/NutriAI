package com.nexon.nutriai.ai.vector_store;

import com.nexon.nutriai.config.properties.LanceDBProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ReactiveLanceDBClient {

    private final WebClient webClient;
    private final String defaultTableName;

    public ReactiveLanceDBClient(LanceDBProperties lanceDBProperties) {
        this.webClient = WebClient.builder()
                .baseUrl("http://" + lanceDBProperties.getHost() + ":" + lanceDBProperties.getPort())
                .build();
        this.defaultTableName = "documents";
    }

    public Mono<Void> createTable(String tableName) {
        return webClient.post()
                .uri("/tables/" + tableName)
                .retrieve()
                .bodyToMono(String.class)
                .then()
                .doOnSuccess(unused -> System.out.println("Table '" + tableName + "' is ready."))
                .onErrorResume(e -> {
                    System.err.println("Failed to create table: " + e.getMessage());
                    return Mono.empty(); // 或者返回错误
                });
    }

    public Mono<Void> addDocument(String tableName, String id, String content, List<Double> vector) {
        Map<String, Object> doc = Map.of("id", id, "content", content, "vector", vector);
        return webClient.post()
                .uri("/tables/" + tableName + "/add")
                .bodyValue(List.of(doc))
                .retrieve()
                .bodyToMono(String.class)
                .then()
                .doOnSuccess(unused -> System.out.println("Added document to table '" + tableName + "'."));
    }
    
    public Mono<Void> addDocuments(String tableName, List<DocumentData> documents) {
        List<Map<String, Object>> docs = documents.stream()
                .map(doc -> Map.of(
                        "id", doc.getId(),
                        "content", doc.getContent(),
                        "vector", doc.getVector(),
                        "metadata", doc.getMetadata()))
                .toList();
        
        return webClient.post()
                .uri("/tables/" + tableName + "/add")
                .bodyValue(docs)
                .retrieve()
                .bodyToMono(String.class)
                .then()
                .doOnSuccess(unused -> System.out.println("Added " + documents.size() + " documents to table '" + tableName + "'."));
    }
    
    public Mono<Void> deleteDocuments(String tableName, List<String> ids) {
        Map<String, Object> request = Map.of("ids", ids);
        return webClient.post()
                .uri("/tables/" + tableName + "/delete")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .then()
                .doOnSuccess(response -> System.out.println("Deleted " + ids.size() + " documents from table '" + tableName + "'."))
                .onErrorResume(e -> {
                    System.err.println("Failed to delete documents from LanceDB: " + e.getMessage());
                    return Mono.empty();
                });
    }

    public Flux<SearchResult> search(String tableName, List<Double> queryVector, int limit) {
        Map<String, Object> request = Map.of("query_vector", queryVector, "limit", limit);
        return webClient.post()
                .uri("/tables/" + tableName + "/search")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(SearchResult.class)
                .doOnNext(r -> System.out.printf("ID: %s, Content: %s, Score: %.4f%n", r.getId(), r.getContent(), r.getScore()))
                .doOnError(e -> System.err.println("Search failed: " + e.getMessage()));
    }
    
    public Flux<SearchResult> search(String tableName, List<Double> queryVector) {
        return search(tableName, queryVector, 3);
    }
    
    // Document DTO
    public static class DocumentData {
        private String id;
        private String content;
        private List<Double> vector;
        private Map<String, Object> metadata;
        
        public DocumentData() {}
        
        public DocumentData(String id, String content, List<Double> vector, Map<String, Object> metadata) {
            this.id = id;
            this.content = content;
            this.vector = vector;
            this.metadata = metadata;
        }
        
        // Getters and Setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public List<Double> getVector() {
            return vector;
        }
        
        public void setVector(List<Double> vector) {
            this.vector = vector;
        }
        
        public Map<String, Object> getMetadata() {
            return metadata;
        }
        
        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    // SearchResult DTO
    public static class SearchResult {
        private String id;
        private String content;
        private Map<String, Object> metadata;
        private double score;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
        
        public Map<String, Object> getMetadata() {
            return metadata;
        }
        
        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }
}
