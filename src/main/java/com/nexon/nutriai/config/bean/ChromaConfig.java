package com.nexon.nutriai.config.bean;

import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChromaConfig {

    @Bean
    public ChromaApi chromaApi() {
        return ChromaApi.builder().build();
    }
}
