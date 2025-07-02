package com.example.moyeorak.config;

import com.example.moyeorak.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info", "/health").permitAll() // 헬스체크 페이지 누구나 접근 가능
                        .requestMatchers("/api/users/signup", "/api/users/login", "/error").permitAll()  // 회원가입, 로그인, 에러 페이지는 누구나 접근 가능
                        .requestMatchers("/api/users/delete", "/api/users/check-email", "/api/users/check-phone").permitAll()  // 회원 탈퇴는 누구나 접근 가능
                        .requestMatchers("/api/rentals", "/api/rentals/{id}").permitAll()
                        .anyRequest().authenticated()  // 그 외에는 인증된 사용자만 접근 가능
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
