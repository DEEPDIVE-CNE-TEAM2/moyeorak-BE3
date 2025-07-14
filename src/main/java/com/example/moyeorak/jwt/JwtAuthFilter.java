package com.example.moyeorak.jwt;

import com.example.moyeorak.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmail(token);
                String role = jwtProvider.getRole(token); // 🔥 토큰에서 roles 클레임 추출

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    String authority = "ROLE_" + role.toUpperCase(); // Spring Security는 ROLE_ 접두어 필요
                    var authorities = List.of(new SimpleGrantedAuthority(authority));

                    // 사용자 정보 객체 생성
                    var userDetails = new CustomUserDetails(
                            null,  // 사용자 ID는 DB 조회를 하지 않기 때문에 null로 처리
                            email,
                            role,
                            authorities
                    );

                    // 인증 객체 생성 후 등록
                    var auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.debug("✅ JWT 인증 성공 - 사용자: {}, 권한: {}", email, role);
                }
            } else {
                log.warn("❌ 유효하지 않은 JWT 토큰: {}", token);
            }
        }

        filterChain.doFilter(request, response);
    }
}
