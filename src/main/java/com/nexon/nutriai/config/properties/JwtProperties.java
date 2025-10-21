package com.nexon.nutriai.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties("app.jwt")
public class JwtProperties {
    private List<String> excludePaths = new ArrayList<>();

    private String secret;
}
