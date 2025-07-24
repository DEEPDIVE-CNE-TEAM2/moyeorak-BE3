package com.example.moyeorak.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ✅ 액세스 토큰 생성
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000L * 60 * 30); // 30분

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ 리프레시 토큰 생성
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000L * 60 * 30); // 30분

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ 이메일 추출
    public String getEmail(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (Exception e) {
            log.warn("이메일 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // ✅ 권한(role) 추출
    public String getRole(String token) {
        try {
            return parseClaims(token).get("roles", String.class);
        } catch (Exception e) {
            log.warn("권한 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // ✅ 토큰 유효성 검증 (예외는 상위로 throw하여 JwtAuthFilter에서 처리)
    public boolean validateToken(String token) {
        parseClaims(token); // 여기서 예외 발생 시 그대로 위로 던짐
        return true;
    }

    // ✅ 토큰에서 Claims 파싱
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ 요청 헤더에서 Bearer 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
