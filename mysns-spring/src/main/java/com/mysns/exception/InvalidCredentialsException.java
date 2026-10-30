package com.mysns.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("아이디 또는 비밀번호가 일치하지 않습니다.");
    }
}
