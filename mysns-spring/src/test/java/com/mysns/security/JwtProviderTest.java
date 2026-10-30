package com.mysns.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-0123456789";

    private JwtProvider provider(String secret, Duration accessTtl) {
        return new JwtProvider(new JwtProperties(secret, accessTtl, Duration.ofDays(7), false));
    }

    @Test
    void access_토큰에서_사용자_아이디를_꺼낸다() {
        JwtProvider provider = provider(SECRET, Duration.ofMinutes(15));
        String token = provider.createAccessToken("a@b.com");
        assertEquals(Optional.of("a@b.com"), provider.parseAccessToken(token));
    }

    @Test
    void refresh_토큰은_access_토큰으로_쓸_수_없고_그_반대도_마찬가지다() {
        JwtProvider provider = provider(SECRET, Duration.ofMinutes(15));
        String refresh = provider.createRefreshToken("a@b.com", "jti-1");
        String access = provider.createAccessToken("a@b.com");

        assertTrue(provider.parseAccessToken(refresh).isEmpty());
        assertTrue(provider.parseRefreshToken(access).isEmpty());
        assertEquals("jti-1", provider.parseRefreshToken(refresh).orElseThrow().jti());
    }

    @Test
    void 만료된_토큰은_거부한다() {
        JwtProvider provider = provider(SECRET, Duration.ofMillis(-1000));
        assertTrue(provider.parseAccessToken(provider.createAccessToken("a@b.com")).isEmpty());
    }

    @Test
    void 다른_키로_서명했거나_변조된_토큰은_거부한다() {
        JwtProvider provider = provider(SECRET, Duration.ofMinutes(15));
        JwtProvider other = provider("another-secret-another-secret-another-secret-99", Duration.ofMinutes(15));

        String forged = other.createAccessToken("a@b.com");
        assertTrue(provider.parseAccessToken(forged).isEmpty());

        String tampered = provider.createAccessToken("a@b.com") + "x";
        assertTrue(provider.parseAccessToken(tampered).isEmpty());
        assertTrue(provider.parseAccessToken("not-a-jwt").isEmpty());
    }

    @Test
    void 비밀키가_없거나_짧으면_시작하지_못한다() {
        assertThrows(IllegalStateException.class, () -> provider(null, Duration.ofMinutes(15)));
        assertThrows(IllegalStateException.class, () -> provider("", Duration.ofMinutes(15)));
        assertThrows(IllegalStateException.class, () -> provider("too-short", Duration.ofMinutes(15)));
    }
}
