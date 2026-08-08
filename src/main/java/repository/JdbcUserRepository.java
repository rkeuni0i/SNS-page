package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.naming.NamingException;

import domain.User;
import exceptions.DataAccessException;
import exceptions.DuplicateUserException;
import util.ConnectionPool;

/**
 * 기존 UserDAO를 대체한다. 주요 개선점
 *
 *  1) try-with-resources로 Connection/PreparedStatement/ResultSet을 자동 반환한다.
 *     기존 finally 방식은 close 순서를 사람이 매번 올바르게 써야 했고,
 *     close() 자체가 던지는 예외가 원래 예외를 덮어쓰는 문제도 있었다.
 *  2) SELECT * 대신 필요한 컬럼만 명시한다. 운영 중 컬럼이 추가/변경되어도
 *     매핑이 조용히 깨지지 않는다.
 *  3) 체크 예외(SQLException/NamingException)를 DataAccessException으로 감싸
 *     상위 계층이 JDBC를 알 필요가 없게 한다.
 *  4) UNIQUE 위반은 DB가 최종적으로 막아주므로, 그 신호를 도메인 예외로 번역한다
 *     (아래 save 참고 — 체크-후-삽입 사이의 경쟁 조건 대비).
 */
public class JdbcUserRepository implements UserRepository {

    @Override
    public void save(User user) {
        String sql = "INSERT INTO user(id, password_hash, salt, name) VALUES(?,?,?,?)";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getId());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getSalt());
            stmt.setString(4, user.getName());
            stmt.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            // 서비스 계층의 existsById 검사와 INSERT 사이에 다른 요청이 같은 아이디를
            // 선점하는 경쟁 조건(TOCTOU)이 존재한다. 최종 방어선은 PK 제약이므로,
            // 그 위반을 도메인 예외로 번역해 사용자에게 올바른 메시지를 보여준다.
            throw new DuplicateUserException(user.getId());
        } catch (SQLException | NamingException e) {
            throw new DataAccessException("회원 저장에 실패했습니다: " + user.getId(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM user WHERE id = ?";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException | NamingException e) {
            throw new DataAccessException("회원 존재 여부 조회에 실패했습니다: " + id, e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        String sql = "SELECT id, password_hash, salt, name, ts FROM user WHERE id = ?";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException | NamingException e) {
            throw new DataAccessException("회원 조회에 실패했습니다: " + id, e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM user WHERE id = ?";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException | NamingException e) {
            throw new DataAccessException("회원 탈퇴에 실패했습니다: " + id, e);
        }
    }

    @Override
    public List<User> findAllOrderByCreatedAtDesc() {
        String sql = "SELECT id, password_hash, salt, name, ts FROM user ORDER BY ts DESC";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // 커넥션이 살아있는 스코프 안에서 결과를 전부 리스트로 옮겨 담은 뒤 반환한다.
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;

        } catch (SQLException | NamingException e) {
            throw new DataAccessException("회원 목록 조회에 실패했습니다.", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("ts");
        return User.builder()
                .id(rs.getString("id"))
                .name(rs.getString("name"))
                .passwordHash(rs.getString("password_hash"))
                .salt(rs.getString("salt"))
                .createdAt(ts != null ? ts.toLocalDateTime() : null)
                .build();
    }
}
