package com.example.moyeorak.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
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
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    /** 로컬/개발에서만 임시 우회 허용 (기본 false) */
    @Value("${security.jwt.dev-bypass:false}")
    private boolean devBypass;

    /** 스웨거/헬스/정적 리소스 등은 필터를 건너뜀 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/actuator")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-ui.html");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        try {
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if (jwtProvider.validateToken(token)) {
                    // ---- 여기부터 완화된 인증 세팅 ----
                    String email = jwtProvider.getEmail(token);
                    if (email == null) {
                        // sub를 이메일로 사용하는 케이스 대응
                        email = jwtProvider.getSubject(token);
                    }

                    Long userId = jwtProvider.getUserId(token);
                    if (userId == null) userId = -1L; // placeholder

                    String roleRaw = jwtProvider.getRole(token);
                    if (roleRaw == null || roleRaw.isBlank()) roleRaw = "USER";

                    var authorities = Arrays.stream(roleRaw.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase())
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        var principal = new SimpleUserPrincipal(userId, email);
                        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("✅ JWT 인증 성공 - userId: {}, email: {}, roles: {}", userId, email, authorities);
                    }
                } else {
                    if (devBypass) {
                        bypassAsTempUser(request, "검증실패(개발우회)");
                    } else {
                        response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다.");
                        return;
                    }
                }

            } else {
                if (devBypass) {
                    bypassAsTempUser(request, "헤더없음/비Bearer(개발우회)");
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            if (devBypass) {
                bypassAsTempUser(request, "만료(개발우회)");
                filterChain.doFilter(request, response);
            } else {
                log.warn("❌ AccessToken 만료: {}", e.getMessage());
                response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\", error_description=\"expired\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "AccessToken이 만료되었습니다.");
            }
        } catch (SignatureException | MalformedJwtException e) {
            if (devBypass) {
                bypassAsTempUser(request, "서명/형식 오류(개발우회)");
                filterChain.doFilter(request, response);
            } else {
                log.warn("❌ 잘못된 JWT 서명 또는 토큰 형식: {}", e.getMessage());
                response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다.");
            }
        } catch (Exception e) {
            if (devBypass) {
                bypassAsTempUser(request, "기타오류(개발우회)");
                filterChain.doFilter(request, response);
            } else {
                log.warn("❌ JWT 검증 중 기타 오류: {}", e.getMessage());
                response.setHeader("WWW-Authenticate", "Bearer");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 인증에 실패했습니다.");
            }
        }
    }

    private void bypassAsTempUser(HttpServletRequest request, String reason) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var principal = new SimpleUserPrincipal(-1L, "temp@local");

        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        log.warn("⚠️ JWT 우회 인증 적용 [{}] - tempUser로 진행", reason);
    }

    /** 컨트롤러에서 @AuthenticationPrincipal(expression = "id") / ("email") 로 접근 가능한 Principal */
    public record SimpleUserPrincipal(Long id, String email) implements Principal {
        @Override public String getName() { return email != null ? email : String.valueOf(id); }
    }
}
