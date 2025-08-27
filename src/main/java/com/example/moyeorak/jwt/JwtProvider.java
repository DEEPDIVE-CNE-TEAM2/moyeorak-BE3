package com.example.moyeorak.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
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

            // sub가 숫자면 그것도 허용
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

    /** 단일 role 클레임 또는 roles[0] */
    public String getRole(String token) {
        try {
            Claims c = parseClaims(token);
            String role = claimAsString(c, "role");
            if (role != null) return role;

            Object roles = c.get("roles");
            if (roles instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                return first == null ? null : String.valueOf(first);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** (선택) regionId 클레임을 쓰고 싶다면 사용 */
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
        var parser = Jwts.parserBuilder().setSigningKey(key).build();
        var jws = parser.parseClaimsJws(token);
        Claims c = jws.getBody();

        // issuer가 설정되어 있으면 검사(선택)
        if (issuer != null && !issuer.isBlank()) {
            if (!Objects.equals(issuer, c.getIssuer())) {
                throw new JwtException("Invalid issuer");
            }
        }
        // 만료는 JJWT가 ExpiredJwtException으로 이미 처리
        Date exp = c.getExpiration();
        if (exp != null && exp.before(new Date())) {
            throw new ExpiredJwtException(jws.getHeader(), c, "Expired");
        }
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
