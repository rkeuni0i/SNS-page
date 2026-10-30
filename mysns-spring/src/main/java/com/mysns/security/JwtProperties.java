package com.mysns.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param secret       HS256 서명 키. 32바이트(256비트) 이상이어야 하며 환경변수 JWT_SECRET으로 주입한다.
 * @param cookieSecure refresh 쿠키의 Secure 플래그. HTTPS로 배포하면 반드시 true.
 */
@ConfigurationProperties(prefix = "mysns.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("15m") Duration accessTtl,
        @DefaultValue("7d") Duration refreshTtl,
        @DefaultValue("false") boolean cookieSecure) {
}
