package com.nexon.nutriai.ai.embed.vector_store;

import com.nexon.nutriai.ai.embed.vectorex.NutriDoc;
import com.nexon.nutriai.ai.embed.vectorex.NutriDocMapper;
import com.nexon.nutriai.util.SpringBeanUtils;
import io.github.javpower.vectorexbootstater.core.VectoRexResult;
import io.github.javpower.vectorexcore.entity.VectoRexEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class VectoRexVectorStore implements VectorStore {

    private final NutriDocMapper nutriDocMapper;
    private final EmbeddingModel embeddingModel;

    @Autowired
    public VectoRexVectorStore(NutriDocMapper nutriDocMapper, EmbeddingModel embeddingModel){
        this.nutriDocMapper = nutriDocMapper;
        this.embeddingModel = embeddingModel;
    }

    public VectoRexVectorStore(EmbeddingModel embeddingModel){
        this.nutriDocMapper = SpringBeanUtils.getBean(NutriDocMapper.class);
        this.embeddingModel = embeddingModel;
    }

    @Override
    public String getName() {
        return VectorStore.super.getName();
    }


    @Override
    public void add(List<Document> documents) {
        Objects.requireNonNull(documents, "Documents list cannot be null");
        if (documents.isEmpty()) {
            throw new IllegalArgumentException("Documents list cannot be empty");
        }
        for (Document document : documents) {
            float[] embedding = this.embeddingModel.embed(document);
            NutriDoc nutriDoc = new NutriDoc(document.getId(), document.getText(), embedding, document.getMetadata());
            nutriDocMapper.insert(nutriDoc);
        }
    }

    @Override
    public void delete(List<String> idList) {
        Objects.requireNonNull(idList, "ID list cannot be null");
        if (idList.isEmpty()) {
            throw new IllegalArgumentException("ID list cannot be empty");
        }
        for (String id : idList) {
            nutriDocMapper.removeById(id);
        }
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Document> similaritySearch(String query) {
        return similaritySearch(SearchRequest.builder().query(query).build());
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        float[] embedding = this.embeddingModel.embed(request.getQuery());
        List<VectoRexResult<NutriDoc>> query = nutriDocMapper.queryWrapper().vector("vector", toFloatList(embedding)).topK(2).query();
        if (query != null && !query.isEmpty()) {
            return query.stream()
                    .map(result -> new Document(result.getEntity().getContent(), result.getEntity().getMetadata()))
                    .toList();
        }
        return List.of();
    }

    public void getCollections() {
        List<VectoRexEntity> collections = nutriDocMapper.getClient().getCollections();
        for (VectoRexEntity collection : collections) {
            log.info("Collection: {}", collection.getCollectionName());
        }
    }

    public void delCollection(String collection) {
        nutriDocMapper.getClient().delCollection(collection);
    }

    private static List<Float> toFloatList(float[] array) {
        List<Float> list = new ArrayList<>();
        for (float f : array) {
            list.add(f);
        }
        return list;
    }
}
