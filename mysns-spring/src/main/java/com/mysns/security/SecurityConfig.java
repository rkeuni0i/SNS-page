package com.mysns.security;

import java.io.IOException;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtProvider jwtProvider) throws Exception {
        http
                // 쿠키 세션이 아니라 Authorization 헤더의 토큰으로 인증하므로 서버는 상태를 갖지 않는다.
                // (브라우저가 자동으로 붙이는 인증 정보가 없어 CSRF 공격 대상이 되지 않는다.)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 가입·로그인·토큰 재발급·로그아웃은 로그인 전에 호출한다.
                        .requestMatchers(HttpMethod.POST, "/api/users", "/api/auth/login",
                                "/api/auth/refresh", "/api/auth/logout").permitAll()
                        // React 정적 파일과 화면 경로(SpaController가 index.html로 forward).
                        // <img>는 Authorization 헤더를 보낼 수 없어 /images도 열어 둔다(파일명은 UUID).
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**", "/login", "/signup",
                                "/users", "/users/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 그 외는 전부 인증 필요 — 새 API를 추가해도 기본값은 "막힘"이다.
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        // RFC 6750: 토큰이 없거나 만료된 401에는 WWW-Authenticate를 싣는다.
                        // 프론트엔드는 이 헤더가 있을 때만 토큰 재발급을 시도한다(비밀번호 오류 401과 구분).
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setHeader("WWW-Authenticate", "Bearer");
                            writeError(res, 401, "로그인이 필요합니다.");
                        })
                        .accessDeniedHandler((req, res, ex) -> writeError(res, 403, "접근 권한이 없습니다.")))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void writeError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
