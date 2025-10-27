package com.nexon.nutriai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFlux 配置类
 * 
 * 该类用于配置 WebFlux 相关设置，特别是处理阻塞操作的执行器配置。
 * 在响应式编程中，阻塞操作需要在专用的线程池中执行，以避免阻塞事件循环线程。
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    /**
     * 配置阻塞操作的执行器
     * 
     * 此方法配置了一个 SimpleAsyncTaskExecutor 来处理阻塞操作。
     * SimpleAsyncTaskExecutor 为每个任务创建新线程，适用于处理阻塞 I/O 操作。
     * 线程名称前缀设置为 "blocking-" 以便于调试和监控。
     * 
     * @param configurer 阻塞执行配置器
     */
    @Override
    public void configureBlockingExecution(BlockingExecutionConfigurer configurer) {
        // 使用 SimpleAsyncTaskExecutor 处理阻塞代码
        configurer.setExecutor(new SimpleAsyncTaskExecutor("blocking-"));
    }
}

