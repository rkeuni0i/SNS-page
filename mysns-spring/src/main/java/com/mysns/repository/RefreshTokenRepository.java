package com.mysns.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 발급한 refresh 토큰의 jti를 보관한다. DB에 없는 토큰은 서명이 유효해도 거부하므로
 * 로그아웃·탈퇴·재사용 시 서버에서 즉시 무효화할 수 있다.
 */
@Repository
public class RefreshTokenRepository {

    private final JdbcTemplate jdbc;

    public RefreshTokenRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 만료 시각은 JVM·DB 시간대 차이를 피하려고 DB 시계로 계산한다. */
    public void save(String jti, String userId, long ttlSeconds) {
        jdbc.update("INSERT INTO refresh_token(jti, user_id, expires_at) VALUES (?, ?, TIMESTAMPADD(SECOND, ?, NOW()))",
                jti, userId, ttlSeconds);
    }

    /**
     * 유효한 토큰이면 삭제하고 true를 돌려준다. 조회와 삭제를 한 문장으로 처리하므로
     * 같은 토큰으로 동시에 재발급을 요청해도 한 요청만 성공한다(rotation).
     */
    public boolean consume(String jti, String userId) {
        return jdbc.update("DELETE FROM refresh_token WHERE jti = ? AND user_id = ? AND expires_at > NOW()",
                jti, userId) > 0;
    }

    public void deleteExpired() {
        jdbc.update("DELETE FROM refresh_token WHERE expires_at <= NOW()");
    }
}
