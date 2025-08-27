package com.example.moyeorak.config;

import com.example.moyeorak.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 기본 보안 옵션
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인가 정책
                .authorizeHttpRequests(auth -> auth
                        // Preflight 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 엔드포인트 (헬스/문서)
                        .requestMatchers(
                                "/actuator/health", "/actuator/info", "/actuator/**",
                                "/health",
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/v3/api-docs.yaml"
                        ).permitAll()

                        // 콘텐츠 조회는 공개(GET만)
                        .requestMatchers(HttpMethod.GET,
                                "/api/content/programs/**",
                                "/api/content/notices/**",
                                "/api/content/main-images/**"
                        ).permitAll()

                        // 관리자 전용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 그 외 content 쓰기/수정/삭제는 인증 필요
                        .requestMatchers("/api/content/**").authenticated()

                        // 나머지는 차단 (다른 라우트는 게이트웨이에서 처리)
                        .anyRequest().denyAll()
                )

                // 예외 처리
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
                        })
                )

                // JWT 필터
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 운영에서는 정확한 오리진만 허용
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://goorm-alb-1610121085.ap-northeast-2.elb.amazonaws.com",
                "https://www.moyeorak.cloud",
                "https://moyeorak.cloud",
                "https://api.moyeorak.cloud"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Idempotency-Key"
        ));
        // 파일/다운로드 처리를 위해 노출할 헤더
        config.setExposedHeaders(List.of("Content-Disposition", "Location"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 1시간 preflight 캐시

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
