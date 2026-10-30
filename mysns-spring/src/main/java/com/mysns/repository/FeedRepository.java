package com.mysns.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.mysns.domain.Feed;

@Repository
public class FeedRepository {

    private static final String SELECT_COLUMNS = "no, id, content, images, ts";

    private static final RowMapper<Feed> ROW_MAPPER = (rs, i) -> {
        Timestamp ts = rs.getTimestamp("ts");
        return new Feed(
                rs.getLong("no"),
                rs.getString("id"),
                rs.getString("content"),
                rs.getString("images"),
                ts != null ? ts.toLocalDateTime() : null);
    };

    private final JdbcTemplate jdbc;

    public FeedRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(String authorId, String content, String imageFileName) {
        jdbc.update("INSERT INTO feed(id, content, images) VALUES(?,?,?)", authorId, content, imageFileName);
    }

    public List<Feed> findAllOrderByCreatedAtDesc(int offset, int limit) {
        return jdbc.query("SELECT " + SELECT_COLUMNS + " FROM feed ORDER BY ts DESC, no DESC LIMIT ? OFFSET ?",
                ROW_MAPPER, limit, offset);
    }

    public List<Feed> findByAuthorId(String authorId, int offset, int limit) {
        return jdbc.query("SELECT " + SELECT_COLUMNS
                + " FROM feed WHERE id = ? ORDER BY ts DESC, no DESC LIMIT ? OFFSET ?",
                ROW_MAPPER, authorId, limit, offset);
    }
}
