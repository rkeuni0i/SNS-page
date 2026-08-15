package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import exceptions.DataAccessException;
import util.ConnectionPool;

/**
 * JdbcUserRepository/JdbcFeedRepository와 같은 원칙을 따른다.
 *  - try-with-resources로 자원을 자동 반환한다.
 *  - 커넥션이 열려 있는 스코프 안에서 결과를 자바 컬렉션으로 완전히
 *    옮겨 담은 뒤 반환한다(JdbcFeedRepository 클래스 주석 참고).
 *  - PK/제약 위반처럼 "이미 그 상태였다"는 신호는 예외로 던지지 않고
 *    boolean으로 되돌려준다. 팔로우 버튼을 두 번 눌렀다고 사용자에게
 *    에러 화면을 보여줄 이유가 없기 때문이다.
 */
public class JdbcFollowRepository implements FollowRepository {

    @Override
    public boolean follow(String followerId, String followeeId) {
        String sql = "INSERT INTO follow(follower_id, followee_id) VALUES (?, ?)";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, followerId);
            stmt.setString(2, followeeId);
            stmt.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            // 복합 PK 위반 = 이미 팔로우 중. 경쟁 조건(더블클릭)의 최종 방어선이 여기다.
            return false;
        } catch (SQLException | NamingException e) {
            throw new DataAccessException("팔로우에 실패했습니다: " + followerId + " -> " + followeeId, e);
        }
    }

    @Override
    public boolean unfollow(String followerId, String followeeId) {
        String sql = "DELETE FROM follow WHERE follower_id = ? AND followee_id = ?";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, followerId);
            stmt.setString(2, followeeId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException | NamingException e) {
            throw new DataAccessException("언팔로우에 실패했습니다: " + followerId + " -> " + followeeId, e);
        }
    }

    @Override
    public boolean isFollowing(String followerId, String followeeId) {
        String sql = "SELECT 1 FROM follow WHERE follower_id = ? AND followee_id = ?";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, followerId);
            stmt.setString(2, followeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException | NamingException e) {
            throw new DataAccessException("팔로우 여부 조회에 실패했습니다.", e);
        }
    }

    @Override
    public int countFollowers(String userId) {
        return countWhere("followee_id", userId, "팔로워 수 조회에 실패했습니다: " + userId);
    }

    @Override
    public int countFollowing(String userId) {
        return countWhere("follower_id", userId, "팔로잉 수 조회에 실패했습니다: " + userId);
    }

    private int countWhere(String column, String userId, String errorMessage) {
        String sql = "SELECT COUNT(*) FROM follow WHERE " + column + " = ?";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException | NamingException e) {
            throw new DataAccessException(errorMessage, e);
        }
    }

    @Override
    public List<String> findFollowerIds(String userId) {
        String sql = "SELECT follower_id FROM follow WHERE followee_id = ? ORDER BY ts DESC";
        return queryIds(sql, userId, "팔로워 목록 조회에 실패했습니다: " + userId);
    }

    @Override
    public List<String> findFolloweeIds(String userId) {
        String sql = "SELECT followee_id FROM follow WHERE follower_id = ? ORDER BY ts DESC";
        return queryIds(sql, userId, "팔로잉 목록 조회에 실패했습니다: " + userId);
    }

    private List<String> queryIds(String sql, String userId, String errorMessage) {
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            List<String> ids = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }
            }
            return ids;

        } catch (SQLException | NamingException e) {
            throw new DataAccessException(errorMessage, e);
        }
    }
}
