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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- 공개 엔드포인트 ---
                        .requestMatchers(
                                "/actuator/health", "/actuator/info", "/health",
                                "/api/users/signup", "/api/users/login",
                                "/api/users/check-email", "/api/users/check-phone",
                                "/api/regions/**",
                                "/api/programs/region/**",
                                "/api/users/refresh",
                                "/api/rentals/region/**",
                                "/api/rentals/facilities/region/**",
                                "/api/programs", "/api/programs/{id}",
                                "/api/facilities/{id:[\\d]+}",
                                "/api/facilities/region/{regionId:[\\d]+}",
                                "/api/main-images/region/**",
                                "/api/notices/region/**",
                                "/api/notices/{id}",
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/v3/api-docs.yaml"
                        ).permitAll()

                        // --- CloudWatch 프록시: 기본은 인증 필요 ---
                        .requestMatchers("/api/cloudwatch/**").authenticated()
                        // 만약 공개로 열고 싶으면 위 줄을 주석 처리하고 아래 줄 사용
                        // .requestMatchers("/api/cloudwatch/**").permitAll()

                        // --- 관리자 ---
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // 그 외 전부 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 인증 안 됨 → 401
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 권한 없음 → 403 (기존 401 잘못 반환하던 것 수정)
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 정확한 오리진만 허용 (와일드카드 X)
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
                "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"
        ));
        // 프론트에서 파일/이미지/다운로드 처리 시 필요할 수 있는 헤더 노출
        config.setExposedHeaders(List.of("Content-Disposition", "Location"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 1시간 preflight 캐시

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
