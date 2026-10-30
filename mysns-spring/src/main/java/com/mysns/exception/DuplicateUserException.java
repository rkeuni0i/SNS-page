package com.mysns.exception;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String userId) {
        super("이미 가입된 아이디입니다: " + userId);
    }
}
