package com.youbin.util;

import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌工具
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private byte[] key() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public String createToken(Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        return JWTUtil.createToken(payload, key());
    }

    public Long parseUserId(String token) {
        try {
            if (!JWTUtil.verify(token, key())) {
                return null;
            }
            Object uid = JWTUtil.parseToken(token).getPayload("userId");
            return uid == null ? null : Long.valueOf(uid.toString());
        } catch (Exception e) {
            return null;
        }
    }
}