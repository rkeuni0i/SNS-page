package com.mysns.domain;

import java.time.LocalDateTime;

public record User(String id, String name, String passwordHash, String salt, LocalDateTime createdAt) {

    /** 비밀번호 해시가 로그에 새어나가지 않도록 toString에서 제외한다. */
    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "'}";
    }
}
