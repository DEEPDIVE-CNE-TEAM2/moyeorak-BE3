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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
                        // 공개로 열려면 위 줄 주석 처리하고 아래 줄 사용
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
                            // 권한 없음 → 403
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 정확한 오리진 명시 (크리덴셜 사용 시 * 불가)
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

        // 필요 시 하위 도메인 패턴 허용 (예: https://*.moyeorak.cloud)
        // setAllowedOrigins와 병행 사용 가능. 크리덴셜(true)에서도 패턴 허용됨.
        config.setAllowedOriginPatterns(List.of(
                "https://*.moyeorak.cloud"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // S3 presigned upload/download 등 필요한 헤더만 명시적으로 허용 (x-amz-*, Content-Disposition 등 포함)
        config.setAllowedHeaders(List.of(
                "Content-Type",
                "Authorization",
                "Content-Disposition",
                "Content-Length",
                "ETag",
                "x-amz-acl",
                "x-amz-meta-*",
                "x-amz-content-sha256",
                "x-amz-date",
                "x-amz-security-token",
                "x-amz-user-agent",
                "x-amz-request-id",
                "x-amz-version-id",
                "x-amz-storage-class",
                "x-amz-server-side-encryption",
                "x-amz-server-side-encryption-aws-kms-key-id",
                "x-amz-server-side-encryption-context",
                "x-amz-server-side-encryption-customer-algorithm",
                "x-amz-server-side-encryption-customer-key",
                "x-amz-server-side-encryption-customer-key-MD5"
        ));

        // 프론트에서 필요한 응답 헤더 노출
        config.setExposedHeaders(List.of(
                "Content-Disposition",
                "Location",
                "ETag",
                "x-amz-request-id",
                "x-amz-version-id"
        ));

        // 쿠키/인증 포함 요청 허용 (오리진 * 불가, 위에서 명시/패턴 사용)
        config.setAllowCredentials(true);

        // 프리플라이트 캐시
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
