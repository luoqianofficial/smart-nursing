package com.smartnursing.smartnursingadmin.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    //从配置文件读取
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private long expire;

    //生成Key对象
    private SecretKey getKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expire);

        var builder = Jwts.builder()
                .claim("username", username)
                .issuedAt(now)
                .expiration(expireTime)
                .signWith(getKey());

        // 只有当 userId 不为 null 时才设置 subject
        if (userId != null) {
            builder.subject(String.valueOf(userId));
        }

        return builder.compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new RuntimeException("Token 无效或已过期");
        }
    }
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).get("username", String.class);
    }

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("123456");
        System.out.println(encodedPassword);
    }

}
