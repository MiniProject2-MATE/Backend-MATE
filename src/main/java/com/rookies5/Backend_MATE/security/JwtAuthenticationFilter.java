package com.rookies5.Backend_MATE.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 모든 요청 앞에서 토큰을 검사하는 보안 필터
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. 요청 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰이 있고, 유효하다면 신원 확인 진행
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰이 정상이면 유저 정보(Authentication)를 꺼내옴
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            // Spring Security의 보안 바구니(SecurityContext)에 이 유저를 담아둠
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 다음 검사 단계로 이동
        filterChain.doFilter(request, response);
    }

    // 헤더에서 "Bearer "를 떼고 순수 토큰만 가져오는 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    //admin 살리기
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        //admin 요청은 JWT 필터 안 타게
        return !path.startsWith("/api");
    }
}