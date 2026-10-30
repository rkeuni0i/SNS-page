package com.mysns.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysns.dto.AuthResponse;
import com.mysns.dto.LoginRequest;
import com.mysns.dto.UserResponse;
import com.mysns.security.JwtProperties;
import com.mysns.service.AuthService;
import com.mysns.service.AuthService.TokenPair;
import com.mysns.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "refreshToken";
    private static final String COOKIE_PATH = "/api/auth"; // 재발급·로그아웃 요청에만 브라우저가 쿠키를 보낸다

    private final AuthService authService;
    private final UserService userService;
    private final JwtProperties props;

    public AuthController(AuthService authService, UserService userService, JwtProperties props) {
        this.authService = authService;
        this.userService = userService;
        this.props = props;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest req) {
        return respond(authService.login(req.id(), req.password()));
    }

    /** 쿠키의 refresh 토큰으로 새 access 토큰을 받는다. 새로고침 후 로그인 상태를 복원할 때도 쓴다. */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        return respond(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("", 0).toString())
                .build();
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal String myId) {
        return UserResponse.from(userService.getById(myId));
    }

    private ResponseEntity<AuthResponse> respond(TokenPair tokens) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie(tokens.refreshToken(), authService.refreshTtlSeconds()).toString())
                .body(new AuthResponse(tokens.accessToken(), UserResponse.from(tokens.user())));
    }

    private ResponseCookie cookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(props.cookieSecure())
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .build();
    }
}
