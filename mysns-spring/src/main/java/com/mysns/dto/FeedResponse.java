package com.mysns.dto;

import java.time.LocalDateTime;

import com.mysns.domain.Feed;

/** imageUrl은 이미지가 없으면 null. */
public record FeedResponse(long no, String authorId, String content, String imageUrl, LocalDateTime createdAt) {

    public static FeedResponse from(Feed feed) {
        String imageUrl = feed.imageFileName() == null ? null : "/images/" + feed.imageFileName();
        return new FeedResponse(feed.no(), feed.authorId(), feed.content(), imageUrl, feed.createdAt());
    }
}
