package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.port.SavedQueryRepository;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSavedQueryEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC 实现 SavedQueryRepository
 */
@Repository
public class JdbcSavedQueryRepository implements SavedQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcSavedQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SavedQueryRecord save(String name, String ownerId, String queryJson, boolean isPublic) {
        String id = UUID.randomUUID().toString();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        jdbcTemplate.update(
                "INSERT INTO audit_saved_query (id, name, owner_id, query_json, is_public, created_at, " +
                "create_time, update_time, create_user, update_user) VALUES (?,?,?,?,?,?,?,?,?,?)",
                id, name, ownerId, queryJson, isPublic ? 1 : 0, now,
                now, now, "system", "system"
        );

        return new SavedQueryRecord(id, name, ownerId, queryJson, isPublic, now);
    }

    @Override
    public Optional<SavedQueryRecord> findById(String id) {
        List<SavedQueryRecord> results = jdbcTemplate.query(
                "SELECT * FROM audit_saved_query WHERE id = ?",
                this::mapRow,
                id
        );
        return results.stream().findFirst();
    }

    @Override
    public List<SavedQueryRecord> findByOwner(String ownerId) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_saved_query WHERE owner_id = ? OR is_public = 1 ORDER BY create_time DESC",
                this::mapRow,
                ownerId
        );
    }

    @Override
    public List<SavedQueryRecord> findPublic() {
        return jdbcTemplate.query(
                "SELECT * FROM audit_saved_query WHERE is_public = 1 ORDER BY create_time DESC",
                this::mapRow
        );
    }

    @Override
    public boolean deleteById(String id) {
        int rows = jdbcTemplate.update("DELETE FROM audit_saved_query WHERE id = ?", id);
        return rows > 0;
    }

    private SavedQueryRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new SavedQueryRecord(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("owner_id"),
                rs.getString("query_json"),
                rs.getInt("is_public") == 1,
                rs.getString("created_at")
        );
    }
}