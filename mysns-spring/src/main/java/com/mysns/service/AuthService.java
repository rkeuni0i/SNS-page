package com.mysns.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mysns.domain.User;
import com.mysns.exception.UnauthorizedException;
import com.mysns.exception.UserNotFoundException;
import com.mysns.repository.RefreshTokenRepository;
import com.mysns.security.JwtProperties;
import com.mysns.security.JwtProvider;

@Service
public class AuthService {

    public record TokenPair(String accessToken, String refreshToken, User user) {
    }

    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final JwtProperties props;

    public AuthService(UserService userService, RefreshTokenRepository refreshTokenRepository,
                       JwtProvider jwtProvider, JwtProperties props) {
        this.userService = userService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
        this.props = props;
    }

    public TokenPair login(String id, String rawPassword) {
        User user = userService.login(id, rawPassword);
        refreshTokenRepository.deleteExpired(); // 별도 배치 없이 로그인 때마다 만료분을 정리한다
        return issue(user);
    }

    /** refresh 토큰을 한 번 쓰면 폐기하고 새 토큰 쌍을 발급한다. 이미 쓴 토큰을 다시 내면 거부된다. */
    public TokenPair refresh(String refreshToken) {
        var claims = Optional.ofNullable(refreshToken)
                .flatMap(jwtProvider::parseRefreshToken)
                .orElseThrow(() -> new UnauthorizedException("다시 로그인해 주세요."));

        if (!refreshTokenRepository.consume(claims.jti(), claims.userId())) {
            throw new UnauthorizedException("다시 로그인해 주세요.");
        }
        try {
            return issue(userService.getById(claims.userId()));
        } catch (UserNotFoundException e) {
            throw new UnauthorizedException("다시 로그인해 주세요.");
        }
    }

    /** 이 기기의 refresh 토큰만 폐기한다. 토큰이 없거나 이미 무효여도 조용히 넘어간다. */
    public void logout(String refreshToken) {
        Optional.ofNullable(refreshToken)
                .flatMap(jwtProvider::parseRefreshToken)
                .ifPresent(c -> refreshTokenRepository.consume(c.jti(), c.userId()));
    }

    public long refreshTtlSeconds() {
        return props.refreshTtl().toSeconds();
    }

    private TokenPair issue(User user) {
        String jti = UUID.randomUUID().toString();
        refreshTokenRepository.save(jti, user.id(), refreshTtlSeconds());
        return new TokenPair(
                jwtProvider.createAccessToken(user.id()),
                jwtProvider.createRefreshToken(user.id(), jti),
                user);
    }
}
