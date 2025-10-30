package com.nexon.nutriai.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("milvus.python")
public class MilvusLiteProperties {

    private String scriptPath = "scripts/milvus_lite_server.py";

    private int port = 19530;

    private String dataDir = "./milvus_data";
}
