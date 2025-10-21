package com.nexon.nutriai.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("model-list.dashscope")
public class DashscopeModelProperties extends ModelListProperties {

    private String chat;
}
