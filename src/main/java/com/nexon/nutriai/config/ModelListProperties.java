package com.nexon.nutriai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
public class ModelListProperties {

    private String vision;

    private String text;
}
