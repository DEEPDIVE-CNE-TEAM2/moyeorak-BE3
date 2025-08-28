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
                // 정상적인 Bearer 토큰 경로
                String token = header.substring(7);

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
                } else {
                    // Bearer 였지만 검증 실패
                    if (devBypass) {
                        bypassAsTempUser(request, "검증실패(개발우회)");
                    } else {
                        // 예외 처리: 검증 실패를 401로 응답
                        response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다.");
                        return;
                    }
                }

            } else {
                // Bearer 형식이 아니거나 헤더 없음
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
        // 필요시 권한을 고정값으로 부여 (예: ROLE_USER). 민감 API는 Controller/Method 보안으로 추가 제어 권장
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
