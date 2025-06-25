package com.example.moyeorak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/signup",      // 회원가입 허용
                                "/api/users/**",          // 혹시 다른 경로도 쓸 경우 포함
                                "/error",                 // 에러 페이지 허용
                                "/**"                     // 테스트용 전체 허용 (추후 제거 권장)
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
