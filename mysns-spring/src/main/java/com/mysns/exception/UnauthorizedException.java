package com.mysns.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        this("로그인이 필요합니다.");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
