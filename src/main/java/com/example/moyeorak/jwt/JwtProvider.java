package com.example.moyeorak.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret; // HS256용 비밀키 (Base64 권장, 32바이트 이상)

    @Value("${jwt.issuer:}")
    private String issuer; // 선택값

    @Value("${jwt.access-token.ttl-seconds:1800}")   // 기본 30분
    private long accessTokenTtlSeconds;

    @Value("${jwt.refresh-token.ttl-seconds:1209600}") // 기본 14일
    private long refreshTokenTtlSeconds;

    private Key key;

    @PostConstruct
    void init() {
        // Base64로 우선 시도, 실패하면 raw bytes 사용
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            this.key = Keys.hmacShaKeyFor(decoded);
        } catch (Exception e) {
            byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 32) {
                log.warn("jwt.secret 길이가 짧습니다(>=32 bytes 권장). 현재: {} bytes", raw.length);
            }
            this.key = Keys.hmacShaKeyFor(raw);
        }
    }

    /* ====== Token Generation ====== */

    /** Access Token 생성 (subject=email, roles=role 문자열) */
    public String generateToken(String email, String role) {
        long now = System.currentTimeMillis();
        long expMillis = now + (accessTokenTtlSeconds * 1000L);

        var builder = Jwts.builder()
                .setSubject(email)
                .claim("roles", role)              // (예) "ADMIN"
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expMillis));

        if (issuer != null && !issuer.isBlank()) {
            builder.setIssuer(issuer);
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    /** Refresh Token 생성 (subject=email, 권한 등 부가클레임 없이 긴 만료) */
    public String generateRefreshToken(String email) {
        long now = System.currentTimeMillis();
        long expMillis = now + (refreshTokenTtlSeconds * 1000L);

        var builder = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expMillis));

        if (issuer != null && !issuer.isBlank()) {
            builder.setIssuer(issuer);
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    /* ====== Public API ====== */

    /** 토큰 유효성 검증(서명/만료 등) */
    public boolean validateToken(String token) {
        try {
            parseClaims(token); // 파싱되면 유효
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    /** userId 클레임(숫자) 또는 subject가 숫자면 반환 */
    public Long getUserId(String token) {
        try {
            Claims c = parseClaims(token);
            Long id = claimAsLong(c, "id");
            if (id != null) return id;

            id = claimAsLong(c, "userId");
            if (id != null) return id;

            id = claimAsLong(c, "uid");
            if (id != null) return id;

            // sub가 숫자면 허용
            String sub = c.getSubject();
            if (sub != null) {
                try { return Long.parseLong(sub); } catch (NumberFormatException ignored) {}
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** email 클레임 또는 subject(이메일 형태면) */
    public String getEmail(String token) {
        try {
            Claims c = parseClaims(token);
            String email = claimAsString(c, "email");
            if (email != null) return email;

            String sub = c.getSubject();
            if (sub != null && sub.contains("@")) return sub;

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** subject 그대로 반환 */
    public String getSubject(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /** 단일 role 또는 roles 첫 번째 값(문자열/리스트/배열 모두 지원) */
    public String getRole(String token) {
        try {
            Claims c = parseClaims(token);

            // 1) role(단수)
            String role = claimAsString(c, "role");
            if (role != null) return role;

            // 2) roles(복수)
            Object roles = c.get("roles");
            if (roles == null) return null;

            if (roles instanceof String s) {
                // "ADMIN" 또는 "ADMIN,USER"
                String first = s.split(",")[0].trim();
                return first.isEmpty() ? null : first;
            }
            if (roles instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                return first == null ? null : String.valueOf(first).trim();
            }
            if (roles.getClass().isArray()) {
                int len = java.lang.reflect.Array.getLength(roles);
                Object first = len > 0 ? java.lang.reflect.Array.get(roles, 0) : null;
                return first == null ? null : String.valueOf(first).trim();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** (선택) regionId 클레임 */
    public Long getRegionId(String token) {
        try {
            Claims c = parseClaims(token);
            return claimAsLong(c, "regionId");
        } catch (Exception e) {
            return null;
        }
    }

    /** Authorization: Bearer ... 헤더에서 토큰만 추출 */
    public String resolveToken(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h == null) return null;
        h = h.trim();
        if (h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return h.substring(7).trim();
        }
        return null;
    }

    /* ====== Internal ====== */

    private Claims parseClaims(String token) {
        // ❷ Clock skew 허용(±300초)
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(300)
                .build();

        Jws<Claims> jws = parser.parseClaimsJws(token);
        Claims c = jws.getBody();

        // issuer가 설정되어 있으면 검사(선택)
        if (issuer != null && !issuer.isBlank()) {
            if (!Objects.equals(issuer, c.getIssuer())) {
                throw new JwtException("Invalid issuer");
            }
        }
        // 만료(exp) 체크는 JJWT가 이미 처리하므로 수동 재검사 불필요
        return c;
    }

    private static Long claimAsLong(Claims c, String name) {
        Object v = c.get(name);
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (NumberFormatException e) { return null; }
    }

    private static String claimAsString(Claims c, String name) {
        Object v = c.get(name);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }
}
