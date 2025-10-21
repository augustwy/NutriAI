package com.nexon.nutriai.config.bean;

import com.nexon.nutriai.util.cache.Cache;
import com.nexon.nutriai.util.cache.InMemoryCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public <K, V> Cache<K, V> cache() {
        return new InMemoryCache<>();
    }
}
