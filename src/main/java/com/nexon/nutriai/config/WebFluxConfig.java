package com.nexon.nutriai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void configureBlockingExecution(BlockingExecutionConfigurer configurer) {
        // 使用 SimpleAsyncTaskExecutor 处理阻塞代码
        configurer.setExecutor(new SimpleAsyncTaskExecutor("blocking-"));
    }
}

