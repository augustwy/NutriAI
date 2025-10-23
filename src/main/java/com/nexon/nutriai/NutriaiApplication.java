package com.nexon.nutriai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.nexon.nutriai.repository")
public class NutriaiApplication {
    public static void main(String[] args) {
        SpringApplication.run(NutriaiApplication.class, args);
    }
}