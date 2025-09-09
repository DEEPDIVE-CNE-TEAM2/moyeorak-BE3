package com.example.moyeorak.jwt;

import com.example.moyeorak.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final long UNKNOWN_USER_ID = -1L;

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Value("${security.jwt.dev-bypass:false}")
    private boolean devBypass;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ❶ Preflight(OPTIONS)는 즉시 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;
        try {
            token = jwtProvider.resolveToken(request);

            if (token != null && jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmail(token);
                if (email == null) {
                    // sub 사용 케이스
                    email = jwtProvider.getSubject(token);
                }

                String role = jwtProvider.getRole(token);
                if (role == null || role.isBlank()) {
                    role = "USER";
                }

                var authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                var authorities = List.of(authority);

                Long userId = jwtProvider.getUserId(token);
                if (userId == null) {
                    userId = UNKNOWN_USER_ID;
                }

                // Optional: user 로드(없으면 null)
                var user = (email != null) ? userRepository.findByEmail(email).orElse(null) : null;

                var principal = (user != null) ? user : email; // fallback: email 문자열
                var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                if (log.isDebugEnabled()) {
                    log.debug("✅ JWT authentication successful - userId: {}, email: {}, roles: {}",
                            userId, email, authorities);
                }
            } else {
                if (token != null) {
                    log.warn("JWT validation failed or token is null/invalid");
                }
                if (devBypass && SecurityContextHolder.getContext().getAuthentication() == null) {
                    bypassAsTempUser(request, "Validation failed (dev bypass)");
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: sub={}, exp={}",
                    e.getClaims() != null ? e.getClaims().getSubject() : null,
                    e.getClaims() != null ? e.getClaims().getExpiration() : null);
            if (devBypass) {
                bypassAsTempUser(request, "Expired (dev bypass)");
            }
        } catch (Exception e) {
            log.warn("JWT validation error: {}", e.getMessage());
            if (devBypass) {
                bypassAsTempUser(request, "Signature/format error (dev bypass)");
            }
        }

        // 무토큰/비정상 포맷 + devBypass인 경우
        if (token == null && devBypass && SecurityContextHolder.getContext().getAuthentication() == null) {
            bypassAsTempUser(request, "No header/non-Bearer (dev bypass)");
        }

        filterChain.doFilter(request, response);
    }

    private void bypassAsTempUser(HttpServletRequest request, String reason) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_GUEST"));
        var auth = new UsernamePasswordAuthenticationToken("temp-user", null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.debug("⚠️ Dev bypass applied - reason: {}", reason);
    }
}
