package com.mysns.repository;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mysns.domain.User;

@Repository
public class FollowRepository {

    private static final String USER_COLUMNS = "u.id, u.password_hash, u.salt, u.name, u.ts";

    private final JdbcTemplate jdbc;

    public FollowRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** @return 새로 팔로우했으면 true, 이미 팔로우 중이면 false */
    public boolean follow(String followerId, String followeeId) {
        try {
            jdbc.update("INSERT INTO follow(follower_id, followee_id) VALUES (?, ?)", followerId, followeeId);
            return true;
        } catch (DuplicateKeyException e) {
            // 복합 PK 위반 = 이미 팔로우 중(더블클릭 등). 에러로 취급하지 않는다.
            return false;
        }
    }

    public boolean unfollow(String followerId, String followeeId) {
        return jdbc.update("DELETE FROM follow WHERE follower_id = ? AND followee_id = ?",
                followerId, followeeId) > 0;
    }

    public boolean isFollowing(String followerId, String followeeId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM follow WHERE follower_id = ? AND followee_id = ?",
                Integer.class, followerId, followeeId);
        return count != null && count > 0;
    }

    public int countFollowers(String userId) {
        return count("followee_id", userId);
    }

    public int countFollowing(String userId) {
        return count("follower_id", userId);
    }

    /** userId를 팔로우하는 회원들. 기존의 N+1 조회를 JOIN 한 번으로 바꿨다. */
    public List<User> findFollowers(String userId) {
        return jdbc.query("SELECT " + USER_COLUMNS + " FROM follow f JOIN user u ON u.id = f.follower_id"
                + " WHERE f.followee_id = ? ORDER BY f.ts DESC", UserRepository.ROW_MAPPER, userId);
    }

    public List<User> findFollowing(String userId) {
        return jdbc.query("SELECT " + USER_COLUMNS + " FROM follow f JOIN user u ON u.id = f.followee_id"
                + " WHERE f.follower_id = ? ORDER BY f.ts DESC", UserRepository.ROW_MAPPER, userId);
    }

    private int count(String column, String userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM follow WHERE " + column + " = ?",
                Integer.class, userId);
        return count == null ? 0 : count;
    }
}
