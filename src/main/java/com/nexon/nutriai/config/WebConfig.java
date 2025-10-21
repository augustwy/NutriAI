package com.nexon.nutriai.config;

import com.nexon.nutriai.config.interceptor.HeaderInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final HeaderInterceptor headerInterceptor;
    
    public WebConfig(HeaderInterceptor headerInterceptor) {
        this.headerInterceptor = headerInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(headerInterceptor)
                .addPathPatterns("/web/**"); // 应用到所有路径
    }
}
