package com.mysns.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authorization: Bearer 헤더의 access 토큰을 검증해 SecurityContext에 로그인 사용자를 넣는다.
 * 토큰이 없거나 유효하지 않으면 인증 없이 통과시키고, 보호된 경로라면 뒤쪽 필터가 401로 막는다.
 * (빈으로 등록하면 서블릿 필터로 한 번 더 등록되므로 SecurityConfig에서 직접 생성한다.)
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)) {
            jwtProvider.parseAccessToken(header.substring(BEARER.length())).ifPresent(userId ->
                    SecurityContextHolder.getContext().setAuthentication(
                            UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of())));
        }
        chain.doFilter(request, response);
    }
}
