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

        String token = null;
        try {
            token = jwtProvider.resolveToken(request);

            if (token != null && jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmail(token);
                if (email == null) {
                    // Handle case where sub is used as email
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
                    userId = UNKNOWN_USER_ID; // explicit placeholder
                }

                // Optional: load user to enrich context (safe to be null)
                var user = (email != null) ? userRepository.findByEmail(email).orElse(null) : null;

                var principal = (user != null) ? user : email; // fallback to email string
                var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("✅ JWT authentication successful - userId: {}, email: {}, roles: {}",
                        userId, email, authorities);
            } else {
                if (devBypass) {
                    bypassAsTempUser(request, "Validation failed (dev bypass)");
                }
            }
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            if (devBypass) {
                bypassAsTempUser(request, "Expired (dev bypass)");
            }
        } catch (Exception e) {
            log.debug("JWT parse/validation error: {}", e.getMessage());
            if (devBypass) {
                bypassAsTempUser(request, "Signature/format error (dev bypass)");
            }
        }

        if (token == null && devBypass && SecurityContextHolder.getContext().getAuthentication() == null) {
            // No header/non-Bearer etc.
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
