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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

/**
 * JWT만으로 인증 컨텍스트를 구성하는 필터.
 * - DB 조회(UserRepository)나 CustomUserDetails에 의존하지 않는다.
 * - Principal로 SimpleUserPrincipal(id, email)을 넣어 컨트롤러에서
 *   @AuthenticationPrincipal(expression = "id") 로 userId를 바로 추출할 수 있다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

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

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtProvider.validateToken(token)) {

                    Long userId = jwtProvider.getUserId(token);   // id/userId/uid/sub(숫자)에서 추출
                    String email = jwtProvider.getEmail(token);
                    String role  = jwtProvider.getRole(token);

                    if (userId != null && email != null && role != null &&
                            SecurityContextHolder.getContext().getAuthentication() == null) {

                        String authority = "ROLE_" + role.toUpperCase();
                        var authorities = List.of(new SimpleGrantedAuthority(authority));

                        // DB 조회 없이 JWT 클레임으로 principal 구성
                        var principal = new SimpleUserPrincipal(userId, email);

                        var auth = new UsernamePasswordAuthenticationToken(
                                principal, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("✅ JWT 인증 성공 - userId: {}, email: {}, role: {}", userId, email, authority);
                    }
                }
            } catch (ExpiredJwtException e) {
                log.warn("❌ AccessToken 만료: {}", e.getMessage());
                response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\", error_description=\"expired\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "AccessToken이 만료되었습니다.");
                return;
            } catch (SignatureException | MalformedJwtException e) {
                log.warn("❌ 잘못된 JWT 서명 또는 토큰 형식: {}", e.getMessage());
                response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다.");
                return;
            } catch (Exception e) {
                log.warn("❌ JWT 검증 중 기타 오류: {}", e.getMessage());
                response.setHeader("WWW-Authenticate", "Bearer");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 인증에 실패했습니다.");
                return;
            }
        }
        // Authorization 헤더가 없거나 Bearer가 아니면 그대로 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /** 컨트롤러에서 @AuthenticationPrincipal(expression = "id") / ("email") 로 접근 가능한 Principal */
    public record SimpleUserPrincipal(Long id, String email) implements Principal {
        @Override public String getName() { return email != null ? email : String.valueOf(id); }
    }
}
