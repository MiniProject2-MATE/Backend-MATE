package com.rookies5.Backend_MATE.config;

import com.rookies5.Backend_MATE.security.JwtAccessDeniedHandler;
import com.rookies5.Backend_MATE.security.JwtAuthenticationEntryPoint;
import com.rookies5.Backend_MATE.security.JwtAuthenticationFilter;
import com.rookies5.Backend_MATE.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // 1. 비밀번호 암호화 도구
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 실제 인증을 처리할 매니저 (AuthService에서 로그인할 때 사용!)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 3. 보안 필터 체인 조립
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 우리가 만든 예외 처리기 부착
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401 처리
                        .accessDeniedHandler(jwtAccessDeniedHandler) // 403 처리
                )

                // 세션 사용 안 함 (STATELESS)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 토큰 검사 요원(Filter)을 아이디/비밀번호 검사기보다 먼저 배치
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

                // API 명세서 기반 권한 맵핑
                .authorizeHttpRequests(auth -> auth
                        // GUEST (누구나 접근 가능 API)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/check-phone", "/api/users/check-nickname").permitAll()

                        // ADMIN (관리자 전용 API)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 나머지 모든 요청은 USER 이상 (토큰 필수)
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}