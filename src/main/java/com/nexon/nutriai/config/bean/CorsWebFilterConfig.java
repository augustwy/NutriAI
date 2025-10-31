package com.nexon.nutriai.config.bean;

import com.nexon.nutriai.config.properties.CORSOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class CorsWebFilterConfig {

    private final CORSOption corsOption;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 使用配置属性
        config.setAllowedOriginPatterns(corsOption.getAllowedOrigins());
        config.setAllowedMethods(corsOption.getAllowedMethods());
        config.setAllowedHeaders(corsOption.getAllowedHeaders());
        config.setAllowCredentials(corsOption.getAllowCredentials());
        config.setMaxAge(corsOption.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
