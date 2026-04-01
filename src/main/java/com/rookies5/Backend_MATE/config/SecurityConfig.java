package com.rookies5.Backend_MATE.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
<<<<<<< HEAD
                .csrf(csrf -> csrf.disable()) // POST 테스트를 위해 CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 모든 요청을 일단 허용!
                );
=======
            .csrf(csrf -> csrf.disable()) // POST 테스트를 위해 CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // 모든 요청을 일단 허용!
            );
>>>>>>> 2cab6716ed30a22b3115704252d586db8f98f8de
        return http.build();
    }
}