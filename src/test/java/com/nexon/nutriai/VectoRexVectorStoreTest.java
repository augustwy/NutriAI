package com.nexon.nutriai;

import com.nexon.nutriai.ai.vector_store.VectoRexVectorStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NutriaiApplication.class)
public class VectoRexVectorStoreTest {

    @Autowired
    private VectoRexVectorStore vectorStore;

    @Test
    public void getCollections() {
        vectorStore.getCollections();
    }

    @Test
    public void delCollection() {
        vectorStore.delCollection("nutri");
    }
}
