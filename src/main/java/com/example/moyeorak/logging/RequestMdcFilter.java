package com.example.moyeorak.logging;

import com.example.moyeorak.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class RequestMdcFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        // 1) traceId: 헤더가 있으면 사용, 없으면 UUID 발급
        final String traceId = Optional.ofNullable(req.getHeader("X-Trace-Id"))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());
        MDC.put("traceId", traceId);

        try {
            // 2) JWT에서 사용자 정보(가능한 경우만) 추출하여 MDC에 기록
            String token = extractBearer(req);

            if (token != null && jwtProvider.validateToken(token)) {
                // userId / email / role (ROLE_ 접두사 제거)
                try {
                    Long userId = null;
                    try { userId = jwtProvider.getUserId(token); } catch (Exception ignored) {}
                    if (userId != null) MDC.put("userId", String.valueOf(userId));

                    String email = safe(jwtProvider.getEmail(token));
                    if (email != null) MDC.put("email", email);

                    String roleClaim = safe(jwtProvider.getRole(token));
                    if (roleClaim != null) {
                        MDC.put("role", normalizeRole(roleClaim));
                    }

                    // JWT에 regionId 클레임이 있다면 여기도 세팅 (메서드가 없으면 건너뜀)
                    try {
                        var regionIdMethod = jwtProvider.getClass().getMethod("getRegionId", String.class);
                        Object rid = regionIdMethod.invoke(jwtProvider, token);
                        if (rid != null) MDC.put("regionId", String.valueOf(rid));
                    } catch (NoSuchMethodException ignored) {
                        // 프로젝트에 getRegionId가 없다면 무시
                    }
                } catch (Exception e) {
                    // MDC 용도라서 실패해도 요청은 계속
                    log.debug("MDC 채우는 중 JWT 파싱 예외: {}", e.getMessage());
                }
            }

            // 3) 운영자가 헤더로 덮어쓰는 값들 (있을 때만 적용)
            ofHeader(req, "X-User-Id").ifPresent(v -> MDC.put("userId", v));
            ofHeader(req, "X-Email").ifPresent(v -> MDC.put("email", v));
            ofHeader(req, "X-Role").ifPresent(v -> MDC.put("role", normalizeRole(v)));
            ofHeader(req, "X-Region-Id").ifPresent(v -> MDC.put("regionId", v));
            ofHeader(req, "X-Reason").ifPresent(v -> MDC.put("reason", limit(v, 200)));

            chain.doFilter(req, res);
        } finally {
            // 4) 스레드 재사용 오염 방지
            MDC.clear();
        }
    }

    // ───────────── helpers ─────────────

    private static String extractBearer(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h == null) return null;
        h = h.trim();
        if (h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return h.substring(7).trim();
        }
        return null;
    }

    private static Optional<String> ofHeader(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        if (v == null) return Optional.empty();
        v = v.trim();
        return v.isEmpty() ? Optional.empty() : Optional.of(v);
    }

    private static String limit(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max);
    }

    private static String normalizeRole(String role) {
        if (role == null) return null;
        role = role.trim();
        return role.startsWith("ROLE_") ? role.substring(5) : role;
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
