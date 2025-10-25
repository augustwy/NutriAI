package com.nexon.nutriai.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth2.wechat.open")
public class WechatProperties {
    private String appId;
    private String appSecret;
    private String redirectUri;
}