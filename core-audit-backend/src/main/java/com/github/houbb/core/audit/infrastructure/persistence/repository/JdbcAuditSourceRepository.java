package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.enterprise.AuditSource;
import com.github.houbb.core.audit.application.port.AuditSourceRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.AuditSourceConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSourceEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * P9 JDBC 实现 AuditSourceRepository
 */
@Repository
public class JdbcAuditSourceRepository implements AuditSourceRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SourceRowMapper rowMapper = new SourceRowMapper();

    public JdbcAuditSourceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuditSource save(AuditSource source) {
        AuditSourceEntity entity = AuditSourceConverter.toEntity(source);
        String now = now();

        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }

        // upsert: delete then insert
        jdbcTemplate.update("DELETE FROM audit_source WHERE id = ?", entity.getId());

        jdbcTemplate.update(
                "INSERT INTO audit_source (id, name, type, version, tenant, description, endpoint, " +
                "auth_token, status, registered_at, last_seen_at, metadata_json, " +
                "create_time, update_time, create_user, update_user) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getName(), entity.getType(), entity.getVersion(),
                entity.getTenant(), entity.getDescription(), entity.getEndpoint(),
                entity.getAuthToken(), entity.getStatus(),
                entity.getRegisteredAt() != null ? entity.getRegisteredAt() : now,
                entity.getLastSeenAt(),
                entity.getMetadataJson(),
                entity.getCreateTime() != null ? entity.getCreateTime() : now,
                now, "system", "system"
        );

        return findById(entity.getId()).orElse(source);
    }

    @Override
    public Optional<AuditSource> findById(String id) {
        List<AuditSourceEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_source WHERE id = ?", rowMapper, id);
        return results.stream().findFirst().map(AuditSourceConverter::toDomain);
    }

    @Override
    public Optional<AuditSource> findByNameAndTenant(String name, String tenant) {
        List<AuditSourceEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_source WHERE name = ? AND tenant = ?", rowMapper, name, tenant);
        return results.stream().findFirst().map(AuditSourceConverter::toDomain);
    }

    @Override
    public List<AuditSource> findAllByTenant(String tenant) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_source WHERE tenant = ? ORDER BY registered_at DESC",
                rowMapper, tenant).stream().map(AuditSourceConverter::toDomain).toList();
    }

    @Override
    public List<AuditSource> findAllActiveByTenant(String tenant) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_source WHERE tenant = ? AND status = 'ACTIVE' ORDER BY name",
                rowMapper, tenant).stream().map(AuditSourceConverter::toDomain).toList();
    }

    @Override
    public long countByTenant(String tenant) {
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_source WHERE tenant = ?", Long.class, tenant);
        return result != null ? result : 0;
    }

    @Override
    public void updateLastSeenAt(String id, String lastSeenAt) {
        jdbcTemplate.update(
                "UPDATE audit_source SET last_seen_at = ?, update_time = ? WHERE id = ?",
                lastSeenAt, now(), id);
    }

    @Override
    public void deleteById(String id) {
        jdbcTemplate.update("DELETE FROM audit_source WHERE id = ?", id);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private static class SourceRowMapper implements RowMapper<AuditSourceEntity> {
        @Override
        public AuditSourceEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            AuditSourceEntity e = new AuditSourceEntity();
            e.setId(rs.getString("id"));
            e.setName(rs.getString("name"));
            e.setType(rs.getString("type"));
            e.setVersion(rs.getString("version"));
            e.setTenant(rs.getString("tenant"));
            e.setDescription(rs.getString("description"));
            e.setEndpoint(rs.getString("endpoint"));
            e.setAuthToken(rs.getString("auth_token"));
            e.setStatus(rs.getString("status"));
            e.setRegisteredAt(rs.getString("registered_at"));
            e.setLastSeenAt(rs.getString("last_seen_at"));
            e.setMetadataJson(rs.getString("metadata_json"));
            e.setCreateTime(rs.getString("create_time"));
            e.setUpdateTime(rs.getString("update_time"));
            e.setCreateUser(rs.getString("create_user"));
            e.setUpdateUser(rs.getString("update_user"));
            return e;
        }
    }
}