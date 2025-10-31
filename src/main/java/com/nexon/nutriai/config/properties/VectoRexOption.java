package com.nexon.nutriai.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("vectorex")
public record VectoRexOption(String url, String user, String password) {
}
