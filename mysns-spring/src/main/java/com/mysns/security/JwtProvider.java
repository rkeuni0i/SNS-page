package com.mysns.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Access / Refresh JWT의 발급과 검증. 두 토큰은 type 클레임으로 구분해, 서로 대신 쓰지 못하게 한다. */
@Component
public class JwtProvider {

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    public record RefreshClaims(String userId, String jti) {
    }

    private final SecretKey key;
    private final JwtProperties props;

    public JwtProvider(JwtProperties props) {
        if (props.secret() == null || props.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "mysns.jwt.secret(환경변수 JWT_SECRET)이 없거나 32바이트보다 짧습니다.");
        }
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String userId) {
        return build(userId, ACCESS, UUID.randomUUID().toString(), props.accessTtl().toMillis());
    }

    public String createRefreshToken(String userId, String jti) {
        return build(userId, REFRESH, jti, props.refreshTtl().toMillis());
    }

    /** 서명·만료·종류가 모두 유효할 때만 사용자 아이디를 돌려준다. */
    public Optional<String> parseAccessToken(String token) {
        return parse(token, ACCESS).map(Claims::getSubject);
    }

    public Optional<RefreshClaims> parseRefreshToken(String token) {
        return parse(token, REFRESH).map(c -> new RefreshClaims(c.getSubject(), c.getId()));
    }

    private String build(String userId, String type, String jti, long ttlMillis) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId)
                .id(jti)
                .claim(TYPE_CLAIM, type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMillis))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private Optional<Claims> parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return expectedType.equals(claims.get(TYPE_CLAIM, String.class)) ? Optional.of(claims) : Optional.empty();
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
