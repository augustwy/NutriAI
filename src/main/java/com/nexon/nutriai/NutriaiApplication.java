package com.nexon.nutriai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * NutriAI 应用程序主类
 * 
 * 该类是整个应用程序的入口点，负责启动Spring Boot应用。
 * 它启用了配置属性扫描和JPA仓库功能。
 */
@ConfigurationPropertiesScan
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.nexon.nutriai.dao.repository")
public class NutriaiApplication {
    /**
     * 应用程序入口点
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NutriaiApplication.class, args);
    }
}