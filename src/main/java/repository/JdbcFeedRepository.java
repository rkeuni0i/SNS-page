package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import domain.Feed;
import exceptions.DataAccessException;
import util.ConnectionPool;

/**
 * 기존 FeedDAO를 대체한다.
 *
 * [이 클래스가 존재하는 이유 — 실제로 겪은 문제]
 * 원래 main.jsp에는 DAO를 거치지 않고 JSP 안에서 직접
 *     Connection -> PreparedStatement -> ResultSet
 * 을 열어 화면을 그리려던 코드가 주석으로 남아 있었다. 그런데 자원 정리
 * 코드가 stmt/conn을 먼저 닫아버리면 그 뒤의 rs.next()에서
 * "ResultSet is closed" 오류가 난다. ResultSet은 자신을 만든 Statement에,
 * Statement는 다시 Connection에 생명주기가 묶여 있기 때문이다.
 *
 * 해결 원칙: "커넥션이 열려 있는 스코프 안에서 ResultSet을 자바 컬렉션으로
 * 완전히 물질화(materialize)한 뒤 반환한다." 아래 조회 메서드가 모두 이
 * 원칙을 지키므로, 호출부는 커넥션의 생명주기를 전혀 신경 쓸 필요가 없다.
 */
public class JdbcFeedRepository implements FeedRepository {

    private static final String SELECT_COLUMNS = "no, id, content, images, ts";

    @Override
    public void save(Feed feed) {
        String sql = "INSERT INTO feed(id, content, images) VALUES(?,?,?)";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, feed.getAuthorId());
            stmt.setString(2, feed.getContent());
            stmt.setString(3, feed.getImageFileName().orElse(null));
            stmt.executeUpdate();

        } catch (SQLException | NamingException e) {
            throw new DataAccessException("피드 저장에 실패했습니다.", e);
        }
    }

    @Override
    public List<Feed> findAllOrderByCreatedAtDesc(int offset, int limit) {
        // 기존 코드는 "SELECT * FROM feed ORDER BY ts DESC"로 전체 행을 한 번에
        // 가져와, 게시글이 늘어날수록 응답 시간과 힙 사용량이 선형으로 증가했다.
        // LIMIT/OFFSET을 기본 지원해 화면이 필요한 만큼만 읽는다.
        String sql = "SELECT " + SELECT_COLUMNS + " FROM feed ORDER BY ts DESC LIMIT ? OFFSET ?";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            return executeAndMap(stmt);

        } catch (SQLException | NamingException e) {
            throw new DataAccessException("피드 목록 조회에 실패했습니다.", e);
        }
    }

    @Override
    public List<Feed> findByAuthorId(String authorId, int offset, int limit) {
        String sql = "SELECT " + SELECT_COLUMNS
                + " FROM feed WHERE id = ? ORDER BY ts DESC LIMIT ? OFFSET ?";
        try (Connection conn = ConnectionPool.get();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, authorId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            return executeAndMap(stmt);

        } catch (SQLException | NamingException e) {
            throw new DataAccessException("회원별 피드 조회에 실패했습니다: " + authorId, e);
        }
    }

    private List<Feed> executeAndMap(PreparedStatement stmt) throws SQLException {
        List<Feed> feeds = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                feeds.add(mapRow(rs));
            }
        }
        return feeds;
    }

    private Feed mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("ts");
        return Feed.builder()
                .no(rs.getLong("no"))
                .authorId(rs.getString("id"))
                .content(rs.getString("content"))
                .imageFileName(rs.getString("images"))
                .createdAt(ts != null ? ts.toLocalDateTime() : null)
                .build();
    }
}
