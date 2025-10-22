package com.nexon.nutriai.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTPartsParser;
import com.nexon.nutriai.config.properties.JwtProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private Algorithm algorithm;

    private static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000; // 30分钟
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7天

    private final JwtProperties jwtProperties;
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
    }

    public String generateTempToken(String phone) {
        return JWT.create()
                .withClaim("type", "access")
                .withSubject(phone)
                .withIssuer("NutriAI")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000)) // 生成一个1秒的临时token
                .sign(algorithm);
    }

    public Map<String, String> generateToken(String phone) {

        String accessToken  = JWT.create()
                .withClaim("type", "access")
                .withSubject(phone)
                .withIssuer("NutriAI")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .sign(algorithm);

        String refreshToken = JWT.create()
                .withClaim("type", "refresh")
                .withSubject(phone)
                .withIssuer("NutriAI")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .sign(algorithm);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    public boolean validateToken(String token) {
        try {
//            var verifier = JWT.require(algorithm).build();
//            var decodedJWT = verifier.verify(token);
//
//            // 检查token是否过期
//            return !decodedJWT.getExpiresAt().before(new Date());

            JWT.require(algorithm).build().verify(token);
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
        return "refresh".equals(decodedJWT.getClaim("type").asString());
    }
}