package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.diff.Snapshot;
import com.github.houbb.core.audit.application.domain.diff.SnapshotType;
import com.github.houbb.core.audit.application.port.SnapshotRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.DiffConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSnapshotEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditSnapshotRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC 实现 SnapshotRepository
 */
@Repository
public class JdbcSnapshotRepository implements SnapshotRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditSnapshotRowMapper rowMapper = new AuditSnapshotRowMapper();

    public JdbcSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Snapshot save(Snapshot snapshot) {
        AuditSnapshotEntity entity = DiffConverter.toSnapshotEntity(snapshot);

        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateUser("system");
        entity.setUpdateUser("system");

        jdbcTemplate.update(
                "INSERT INTO audit_snapshot (" +
                "id, audit_id, snapshot_type, content_json, created_at, " +
                "create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?)",
                entity.getId(),
                entity.getAuditId(),
                entity.getSnapshotType(),
                entity.getContentJson(),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : now,
                entity.getCreateTime(),
                entity.getUpdateTime(),
                entity.getCreateUser(),
                entity.getUpdateUser()
        );

        return findById(entity.getId()).orElse(snapshot);
    }

    @Override
    public Optional<Snapshot> findById(String id) {
        List<AuditSnapshotEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_snapshot WHERE id = ?",
                rowMapper,
                id
        );
        return results.stream()
                .findFirst()
                .map(DiffConverter::toSnapshotDomain);
    }

    @Override
    public List<Snapshot> findByAuditId(String auditId) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_snapshot WHERE audit_id = ? ORDER BY created_at ASC",
                rowMapper,
                auditId
        ).stream()
                .map(DiffConverter::toSnapshotDomain)
                .toList();
    }

    @Override
    public Optional<Snapshot> findByAuditIdAndType(String auditId, SnapshotType type) {
        List<AuditSnapshotEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_snapshot WHERE audit_id = ? AND snapshot_type = ?",
                rowMapper,
                auditId,
                type.name()
        );
        return results.stream()
                .findFirst()
                .map(DiffConverter::toSnapshotDomain);
    }
}
