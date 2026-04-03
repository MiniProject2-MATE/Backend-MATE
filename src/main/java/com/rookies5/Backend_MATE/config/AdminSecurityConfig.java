package com.rookies5.Backend_MATE.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@Order(1)
public class AdminSecurityConfig {

    private final PasswordEncoder passwordEncoder;

    /**
     * ❌ Bean 제거
     * 👉 그냥 메서드로만 사용
     */
    private UserDetailsService adminUserDetailsService() {

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("1234"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * ✅ Provider (Bean 유지 가능)
     */
    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminUserDetailsService()); // 직접 호출
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * ✅ Security Filter
     */
    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/admin/**")

                .authenticationProvider(adminAuthenticationProvider())

                .csrf(AbstractHttpConfigurer::disable)

                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/logout").permitAll()
                        .anyRequest().hasRole("ADMIN")
                );

        return http.build();
    }
}