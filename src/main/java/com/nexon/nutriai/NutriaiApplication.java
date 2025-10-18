package com.nexon.nutriai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class NutriaiApplication {
    static void main(String[] args) {
        SpringApplication.run(NutriaiApplication.class, args);
    }
}