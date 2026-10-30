package com.mysns.dto;

import java.time.LocalDateTime;

import com.mysns.domain.User;

public record ProfileResponse(
        String id,
        String name,
        LocalDateTime createdAt,
        int followerCount,
        int followingCount,
        boolean following) {

    public static ProfileResponse of(User user, int followerCount, int followingCount, boolean following) {
        return new ProfileResponse(user.id(), user.name(), user.createdAt(), followerCount, followingCount, following);
    }
}
