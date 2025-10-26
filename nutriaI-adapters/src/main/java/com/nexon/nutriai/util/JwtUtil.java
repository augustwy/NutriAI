package com.nexon.nutriai.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nexon.nutriai.config.properties.JwtProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private Algorithm algorithm;

    private static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000; // 30分钟
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7天

    private final JwtProperties jwtProperties;

    @PostConstruct
    public void init() {
        algorithm = Algorithm.HMAC256(jwtProperties.secret());
    }

    public Map<String, String> generateToken(String phone) {

        String accessToken = JWT.create()
                .withClaim("type", "access")
                .withSubject(phone)
                .withIssuer("NutriAI")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .withJWTId(UUIDUtil.generateUUID())
                .sign(algorithm);

        String refreshToken = JWT.create()
                .withClaim("type", "refresh")
                .withSubject(phone)
                .withIssuer("NutriAI")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .withJWTId(UUIDUtil.generateUUID())
                .sign(algorithm);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    public boolean validateToken(String token) {
        try {
            var verifier = JWT.require(algorithm).build();
            var decodedJWT = verifier.verify(token);

            // 永不过期
            String expires = decodedJWT.getClaim("expires").asString();
            if (StringUtils.isNotEmpty(expires) && "never".equals(expires)) {
                return true;
            }
            // 检查token是否过期
            if (decodedJWT.getExpiresAt().before(new Date())) {
                return false;
            }

            // 检查签发时间不是未来时间
            Date issuedAt = decodedJWT.getIssuedAt();
            if (issuedAt != null && issuedAt.after(new Date(System.currentTimeMillis() + 60 * 1000))) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSubjectFromToken(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getSubject();
    }

    public boolean isRefreshToken(String token) {
        DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
        return !"access".equals(decodedJWT.getClaim("type").asString());
    }
}