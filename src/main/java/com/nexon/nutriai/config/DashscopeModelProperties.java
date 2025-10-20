package com.nexon.nutriai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("model-list.dashscope")
public class DashscopeModelProperties extends ModelListProperties {
}
