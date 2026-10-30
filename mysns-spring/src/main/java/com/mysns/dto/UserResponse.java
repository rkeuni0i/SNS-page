package com.mysns.dto;

import java.time.LocalDateTime;

import com.mysns.domain.User;

/** 비밀번호 해시·salt는 응답에 절대 포함하지 않는다. */
public record UserResponse(String id, String name, LocalDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.name(), user.createdAt());
    }
}
