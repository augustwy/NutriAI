package com.nexon.nutriai.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("lancedb.python")
public class LanceDBProperties {

    private String scriptPath = "scripts/lancedb_server.py";

    private String host = "127.0.0.1";

    private int port = 8001;

    private String dbUri = "./data/lancedb_springboot";
}
