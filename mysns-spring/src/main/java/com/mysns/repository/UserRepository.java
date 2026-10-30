package com.mysns.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.mysns.domain.User;
import com.mysns.exception.DuplicateUserException;

@Repository
public class UserRepository {

    static final String SELECT_COLUMNS = "id, password_hash, salt, name, ts";

    static final RowMapper<User> ROW_MAPPER = (rs, i) -> {
        Timestamp ts = rs.getTimestamp("ts");
        return new User(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("password_hash"),
                rs.getString("salt"),
                ts != null ? ts.toLocalDateTime() : null);
    };

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(User user) {
        try {
            jdbc.update("INSERT INTO user(id, password_hash, salt, name) VALUES(?,?,?,?)",
                    user.id(), user.passwordHash(), user.salt(), user.name());
        } catch (DuplicateKeyException e) {
            // existsById 선검사와 INSERT 사이에 같은 아이디가 선점되는 경쟁 조건의 최종 방어선은 PK 제약이다.
            throw new DuplicateUserException(user.id());
        }
    }

    public boolean existsById(String id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM user WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public Optional<User> findById(String id) {
        return jdbc.query("SELECT " + SELECT_COLUMNS + " FROM user WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    /** 비밀번호를 BCrypt 해시로 교체한다. BCrypt는 salt를 해시 문자열에 포함하므로 salt 컬럼은 비운다. */
    public void updatePassword(String id, String bcryptHash) {
        jdbc.update("UPDATE user SET password_hash = ?, salt = NULL WHERE id = ?", bcryptHash, id);
    }

    public boolean deleteById(String id) {
        return jdbc.update("DELETE FROM user WHERE id = ?", id) > 0;
    }

    public List<User> findAllOrderByCreatedAtDesc() {
        return jdbc.query("SELECT " + SELECT_COLUMNS + " FROM user ORDER BY ts DESC", ROW_MAPPER);
    }
}
