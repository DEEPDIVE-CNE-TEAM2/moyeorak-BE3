package com.example.moyeorak.jwt;

import com.example.moyeorak.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmail(token);
                var user = userRepository.findByEmail(email).orElse(null);

                if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name().toUpperCase());

                    var auth = new UsernamePasswordAuthenticationToken(
                            user, null, List.of(authority)
                    );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } else {
                // 개발 시 추적용 로그 (배포 시 삭제 가능)
                System.out.println("Invalid JWT token received");
            }
        }

        filterChain.doFilter(request, response);
    }
}