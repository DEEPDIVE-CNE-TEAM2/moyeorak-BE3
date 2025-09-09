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
    private String secret; // HS256 secret (Base64 recommended, >=32 bytes)

    @Value("${jwt.issuer:}")
    private String issuer; // optional

    @Value("${jwt.access-token.ttl-seconds:1800}")   // default 30m
    private long accessTokenTtlSeconds;

    @Value("${jwt.refresh-token.ttl-seconds:1209600}") // default 14d
    private long refreshTokenTtlSeconds;

    private Key key;

    @PostConstruct
    void init() {
        // Try Base64 first; if fails, fall back to raw bytes
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            this.key = Keys.hmacShaKeyFor(decoded);
        } catch (Exception e) {
            byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 32) {
                log.warn("jwt.secret is short (>=32 bytes recommended). current: {} bytes", raw.length);
            }
            this.key = Keys.hmacShaKeyFor(raw);
        }
    }

    /* ====== Token Generation ====== */

    /** Access Token (subject=email, roles=role string) */
    public String generateToken(String email, String role) {
        long now = System.currentTimeMillis();
        long expMillis = now + (accessTokenTtlSeconds * 1000L);

        var builder = Jwts.builder()
                .setSubject(email)
                .claim("roles", role)              // e.g., "ADMIN"
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expMillis));

        if (issuer != null && !issuer.isBlank()) {
            builder.setIssuer(issuer);
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    /** Refresh Token (subject=email, no extra claims, long expiry) */
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

    /** Validate token (signature/exp/etc.) */
    public boolean validateToken(String token) {
        try {
            parseClaims(token); // parsing succeeds => valid
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    /** userId claim as Long (supports id/userId/uid or numeric sub) */
    public Long getUserId(String token) {
        try {
            Claims c = parseClaims(token);
            Long id = claimAsLong(c, "id");
            if (id != null) return id;

            id = claimAsLong(c, "userId");
            if (id != null) return id;

            id = claimAsLong(c, "uid");
            if (id != null) return id;

            String sub = c.getSubject();
            if (sub != null) {
                try { return Long.parseLong(sub); } catch (NumberFormatException ignored) {}
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** email claim or subject (if looks like email) */
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

    /** Return subject as-is */
    public String getSubject(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /** Single role or first of roles (supports string/list/array) */
    public String getRole(String token) {
        try {
            Claims c = parseClaims(token);

            // 1) role (singular)
            String role = claimAsString(c, "role");
            if (role != null) return role;

            // 2) roles (plural)
            Object roles = c.get("roles");
            if (roles == null) return null;

            if (roles instanceof String s) {
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

    /** (optional) regionId claim */
    public Long getRegionId(String token) {
        try {
            Claims c = parseClaims(token);
            return claimAsLong(c, "regionId");
        } catch (Exception e) {
            return null;
        }
    }

    /** Extract Bearer token from Authorization header */
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
        // ❷ Allow clock skew (±300s) for robust exp/nbf validation
        //    (서버/클라이언트 시간 오차 허용을 위해 ±300초 허용)
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(300)
                .build();

        Jws<Claims> jws = parser.parseClaimsJws(token);
        Claims c = jws.getBody();

        // If issuer is configured, enforce exact match (optional)
        // (issuer가 설정되어 있으면 정확히 일치하는지 검사)
        if (issuer != null && !issuer.isBlank()) {
            if (!Objects.equals(issuer, c.getIssuer())) {
                throw new JwtException("Invalid issuer");
            }
        }

        // Expiration is already validated by JJWT; no manual re-check necessary.
        // (만료(exp) 검증은 JJWT가 이미 수행하므로 수동 재검사가 필요 없습니다)
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
