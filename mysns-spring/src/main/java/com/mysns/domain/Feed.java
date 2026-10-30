package com.mysns.domain;

import java.time.LocalDateTime;

/** imageFileName은 이미지가 없는 글이 정상 케이스이므로 null일 수 있다. */
public record Feed(long no, String authorId, String content, String imageFileName, LocalDateTime createdAt) {
}
