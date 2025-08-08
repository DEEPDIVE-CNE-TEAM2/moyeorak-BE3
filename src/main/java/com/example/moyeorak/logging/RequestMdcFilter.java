package com.example.moyeorak.logging;

import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        // 1. traceId: 요청마다 UUID 하나 발급
        final String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        try {
            // 2. 토큰에서 이메일/권한 뽑기
            String token = jwtProvider.resolveToken(req);
            if (token != null && jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmail(token);      // subject = email
                String roleClaim = jwtProvider.getRole(token);

                // 권한 문자열 정규화: ROLE_ 접두사 제거해서 ADMIN/USER로 통일
                if (roleClaim != null) {
                    String normalized = roleClaim.startsWith("ROLE_") ? roleClaim.substring(5) : roleClaim;
                    MDC.put("role", normalized);
                }

                // 3. 이메일로 DB 조회해서 userId / regionId 세팅
                if (email != null) {
                    userRepository.findByEmail(email).ifPresent(user -> {
                        // userId
                        MDC.put("userId", String.valueOf(user.getId()));

                        // regionId: User가 소속된 지역
                        if (user.getRegion() != null && user.getRegion().getId() != null) {
                            MDC.put("regionId", String.valueOf(user.getRegion().getId()));
                        }
                    });
                }
            }

            // 4. 헤더로 들어오면 reason/regionId 덮어쓰기 (운영자가 특정 사유 남기고 싶을 때)
            ofHeader(req, "X-Reason").ifPresent(v -> MDC.put("reason", limit(v, 200)));
            ofHeader(req, "X-Region-Id").ifPresent(v -> MDC.put("regionId", v));

            chain.doFilter(req, res);
        } finally {
            // 5. 클리어 해서 스레드 재사용 오염 방지
            MDC.clear();
        }
    }

    // 헤더 값 없거나 공백이면 무시
    private static Optional<String> ofHeader(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        if (v == null) return Optional.empty();
        v = v.trim();
        return v.isEmpty() ? Optional.empty() : Optional.of(v);
    }

    // 긴 문자열 자름
    private static String limit(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max);
    }
}