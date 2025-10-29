package com.nexon.nutriai.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("app.models")
public record ModelProperties(Map<String, ServiceMo> providers) {

    public record ServiceMo(String chat, String vision, String text, String embed) {
    }

    public ModelProperties {
        if (providers == null) {
            providers = new HashMap<>();
        }
    }
}
