package com.rookies5.Backend_MATE.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {


    // 3️⃣ 관리 페이지 보안 필터 체인
    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF는 formLogin과 Thymeleaf 연동에서는 기본 사용
            .csrf(AbstractHttpConfigurer::disable)

            // form 기반 로그인 설정
            .formLogin(form -> form
                    .loginPage("/admin/login")         // 관리자 로그인 페이지
                    .loginProcessingUrl("/admin/login") // 로그인 POST URL
                    .defaultSuccessUrl("/admin/dashboard") // 로그인 성공 시 대시보드 이동
                    .usernameParameter("username")     // form input name
                    .passwordParameter("password")
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/admin/logout")
                    .logoutSuccessUrl("/admin/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            )

            // 권한 설정
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/admin/login", "/admin/logout").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            );

        return http.build();
    }
}