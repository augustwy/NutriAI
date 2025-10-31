package com.nexon.nutriai.service.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexon.nutriai.config.properties.WechatOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service("wechatOAuth2Service")
@ConditionalOnProperty(name = "app.oauth2.wechat.enabled", havingValue = "false")
public class WechatOAuth2Service implements OAuth2Service {

    private final WebClient webClient;
    private final WechatOption wechatOption; // 您之前的配置类
    private final ObjectMapper objectMapper;

    public WechatOAuth2Service(WebClient.Builder webClientBuilder, WechatOption wechatOption, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.wechatOption = wechatOption;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderId() {
        return "wechat";
    }

    @Override
    public Mono<JsonNode> getAccessToken(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                wechatOption.getAppId(),
                wechatOption.getAppSecret(),
                code
        );
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(this::parseJson);
    }

    @Override
    public Mono<JsonNode> getUserInfo(String accessToken, String openId) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
                accessToken,
                openId
        );
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(this::parseJson);
    }

    @Override
    public String extractProviderUserId(JsonNode userInfoNode) {
        return userInfoNode.get("openid").asText();
    }

    @Override
    public String extractNickname(JsonNode userInfoNode) {
        return userInfoNode.get("nickname").asText();
    }

    @Override
    public String extractAvatarUrl(JsonNode userInfoNode) {
        return userInfoNode.get("headimgurl").asText();
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response from WeChat", e);
        }
    }
}

