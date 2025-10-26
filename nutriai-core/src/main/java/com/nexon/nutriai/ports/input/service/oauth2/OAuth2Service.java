package com.nexon.nutriai.ports.input.service.oauth2;


import com.nexon.nutriai.domain.service.oauth2.OAuth2AccessToken;
import com.nexon.nutriai.domain.valueobject.ConfigInput;
import com.nexon.nutriai.domain.valueobject.ConfigSnapshot;
import com.nexon.nutriai.ports.output.ResultStream;

/**
 * OAuth2 认证服务的通用接口，所有第三方登录都应实现此接口。
 */
public interface OAuth2Service {

    /**
     * 获取提供商的唯一标识（如 "wechat", "github"）。
     */
    String getProviderId();

    /**
     * 使用授权码换取访问令牌和用户标识。
     *
     * @param code 授权码
     * @return 包含 access_token 和 openid (或 provider_user_id)
     */
    ResultStream<OAuth2AccessToken> getAccessToken(String code);

    /**
     * 使用访问令牌获取用户详细信息。
     *
     * @param accessToken 访问令牌
     * @param openIdOrUserId 用户的唯一标识
     * @return 包含用户信息
     */
    ResultStream<ConfigSnapshot> getUserInfo(String accessToken, String openIdOrUserId);

    /**
     * 从用户信息的 JsonNode 中提取出唯一的用户标识。
     * 不同提供商的字段名可能不同（如微信是 "openid"，GitHub 是 "id"）。
     *
     * @param userInfoNode 用户信息
     * @return 提取出的用户唯一标识
     */
    String extractProviderUserId(ConfigInput userInfoNode);

    /**
     * 从用户信息的 JsonNode 中提取出用户昵称。
     *
     * @param userInfoNode 用户信息
     * @return 提取出的用户昵称
     */
    String extractNickname(ConfigInput userInfoNode);

    /**
     * 从用户信息的 JsonNode 中提取出用户头像URL。
     *
     * @param userInfoNode 用户信息
     * @return 提取出的头像URL
     */
    String extractAvatarUrl(ConfigInput userInfoNode);
}

