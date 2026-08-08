package domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 피드(게시글) 도메인 객체.
 *
 * 이미지가 없는 게시글이 정상 케이스이므로 imageFileName은 Optional로 노출하여
 * 호출부가 null 검사를 잊어 NPE를 내는 상황을 타입 수준에서 방지한다.
 */
public final class Feed {

    private final long no;
    private final String authorId;
    private final String content;
    private final String imageFileName; // nullable
    private final LocalDateTime createdAt;

    private Feed(Builder b) {
        this.no = b.no;
        this.authorId = Objects.requireNonNull(b.authorId, "authorId must not be null");
        this.content = Objects.requireNonNull(b.content, "content must not be null");
        this.imageFileName = b.imageFileName;
        this.createdAt = b.createdAt;
    }

    public long getNo() { return no; }
    public String getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public Optional<String> getImageFileName() { return Optional.ofNullable(imageFileName); }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private long no;
        private String authorId;
        private String content;
        private String imageFileName;
        private LocalDateTime createdAt;

        public Builder no(long no) { this.no = no; return this; }
        public Builder authorId(String authorId) { this.authorId = authorId; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder imageFileName(String imageFileName) { this.imageFileName = imageFileName; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Feed build() { return new Feed(this); }
    }
}
