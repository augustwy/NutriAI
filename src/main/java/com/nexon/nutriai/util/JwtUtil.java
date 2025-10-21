package com.nexon.nutriai.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.nexon.nutriai.config.properties.JwtProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private Algorithm algorithm;

    private final JwtProperties jwtProperties;
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
    }

    public String generateToken(String phone) {
        return JWT.create()
                .withSubject(phone)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .sign(algorithm);
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

    public String getUsernameFromToken(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getSubject();
    }
}