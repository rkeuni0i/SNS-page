package domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 회원 도메인 객체.
 *
 * 불변(Immutable)으로 설계하여 여러 스레드가 동시에 참조해도 안전하며,
 * Builder 패턴으로 생성하여 인자 순서를 헷갈릴 여지를 없앴다.
 *
 * 기존 UserObj와 달리 평문 비밀번호를 보관하지 않고 passwordHash/salt만
 * 들고 있으므로, 실수로 비밀번호가 화면이나 로그에 노출될 여지를 줄인다.
 */
public final class User {

    private final String id;          // 이메일 형태의 로그인 아이디
    private final String name;
    private final String passwordHash;
    private final String salt;
    private final LocalDateTime createdAt;

    private User(Builder b) {
        this.id = Objects.requireNonNull(b.id, "id must not be null");
        this.name = Objects.requireNonNull(b.name, "name must not be null");
        this.passwordHash = b.passwordHash;
        this.salt = b.salt;
        this.createdAt = b.createdAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt() { return salt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String id;
        private String name;
        private String passwordHash;
        private String salt;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder salt(String salt) { this.salt = salt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public User build() { return new User(this); }
    }

    /** 비밀번호 해시가 toString으로 새어나가지 않도록 명시적으로 제외한다. */
    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id.equals(((User) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
