-- 기존 mysns DB(PBKDF2 + 세션 버전)를 BCrypt + JWT 버전으로 올리는 마이그레이션.
-- 데이터를 지우지 않는다. 여러 번 실행해도 안전하다.
SET NAMES utf8mb4;
USE mysns;

-- BCrypt 계정은 salt가 없다(해시 문자열에 포함). 기존 PBKDF2 계정은 salt를 유지하다가
-- 로그인에 성공하는 순간 BCrypt로 다시 저장되며 salt가 NULL이 된다.
ALTER TABLE user MODIFY salt VARCHAR(24) NULL;

CREATE TABLE IF NOT EXISTS refresh_token (
    jti        CHAR(36)     NOT NULL COMMENT 'JWT ID(UUID)',
    user_id    VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    PRIMARY KEY (jti),
    INDEX idx_refresh_user (user_id),
    INDEX idx_refresh_expires (expires_at),
    CONSTRAINT fk_refresh_user
        FOREIGN KEY (user_id) REFERENCES user (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
