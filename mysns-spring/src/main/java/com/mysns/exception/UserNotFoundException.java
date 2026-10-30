package com.mysns.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("존재하지 않는 아이디입니다: " + userId);
    }
}
