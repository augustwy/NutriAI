package com.nexon.nutriai.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("app.jwt")
public record JwtProperties(
        List<String> excludePaths,
        String secret
) {
    public JwtProperties {
        if (excludePaths == null) {
            excludePaths = new ArrayList<>();
        }
    }
}
