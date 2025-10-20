package com.nexon.nutriai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("common")
public class CommonProperties {

    private String filePath;
}
