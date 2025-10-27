package com.nexon.nutriai.config.bean;

import com.alibaba.cloud.ai.memory.jdbc.H2ChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class MemoryConfig {

    @Value("${spring.datasource.url}")
    private String h2Url;
    @Value("${spring.datasource.username}")
    private String h2Username;
    @Value("${spring.datasource.password}")
    private String h2Password;

    @Bean
    public H2ChatMemoryRepository h2ChatMemoryRepository() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(h2Url);
        dataSource.setUsername(h2Username);
        dataSource.setPassword(h2Password);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return H2ChatMemoryRepository.h2Builder()
                .jdbcTemplate(jdbcTemplate)
                .build();
    }
}
