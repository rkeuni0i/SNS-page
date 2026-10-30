package com.mysns.dto;

/** refresh 토큰은 본문에 싣지 않는다 — HttpOnly 쿠키로만 내려가 JavaScript가 읽을 수 없다. */
public record AuthResponse(String accessToken, UserResponse user) {
}
