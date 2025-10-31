package com.nexon.nutriai.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties("app.cors")
public class CORSOption {
    private List<String> allowedOrigins = List.of("*");
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE");
    private List<String> allowedHeaders = List.of("*");
    private Boolean allowCredentials = false;
    private Long maxAge = 3600L;
}
