package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.port.SearchHistoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * JDBC 实现 SearchHistoryRepository
 */
@Repository
public class JdbcSearchHistoryRepository implements SearchHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSearchHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(String userId, String queryJson, String searchedAt) {
        String id = UUID.randomUUID().toString();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        jdbcTemplate.update(
                "INSERT INTO audit_search_history (id, user_id, query_json, searched_at, " +
                "create_time, update_time, create_user, update_user) VALUES (?,?,?,?,?,?,?,?)",
                id, userId, queryJson, searchedAt != null ? searchedAt : now,
                now, now, "system", "system"
        );
    }

    @Override
    public List<SearchHistoryRecord> findRecentByUser(String userId, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_search_history WHERE user_id = ? ORDER BY searched_at DESC LIMIT ?",
                this::mapToRecord,
                userId, limit
        );
    }

    @Override
    public int deleteOlderThan(int days) {
        String cutoff = LocalDate.now().minusDays(days).toString();
        return jdbcTemplate.update(
                "DELETE FROM audit_search_history WHERE searched_at < ?",
                cutoff
        );
    }

    @Override
    public List<PopularQueryRecord> findPopular(int limit) {
        return jdbcTemplate.query(
                "SELECT query_json, COUNT(*) as cnt FROM audit_search_history " +
                "GROUP BY query_json ORDER BY cnt DESC LIMIT ?",
                this::mapToPopular,
                limit
        );
    }

    private SearchHistoryRecord mapToRecord(ResultSet rs, int rowNum) throws SQLException {
        return new SearchHistoryRecord(
                rs.getString("id"),
                rs.getString("user_id"),
                rs.getString("query_json"),
                rs.getString("searched_at")
        );
    }

    private PopularQueryRecord mapToPopular(ResultSet rs, int rowNum) throws SQLException {
        return new PopularQueryRecord(
                rs.getString("query_json"),
                rs.getLong("cnt")
        );
    }
}