package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.compliance.ExportTask;
import com.github.houbb.core.audit.application.port.ExportTaskRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.ComplianceConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditExportTaskEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditExportTaskRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC 实现 ExportTaskRepository
 */
@Repository
public class JdbcExportTaskRepository implements ExportTaskRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditExportTaskRowMapper rowMapper = new AuditExportTaskRowMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public JdbcExportTaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ExportTask save(ExportTask task) {
        AuditExportTaskEntity entity = ComplianceConverter.toExportTaskEntity(task);
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        String now = LocalDateTime.now().format(FORMATTER);
        entity.setCreateTime(entity.getCreateTime() != null ? entity.getCreateTime() : now);
        entity.setUpdateTime(now);
        entity.setCreateUser(entity.getCreateUser() != null ? entity.getCreateUser() : "system");
        entity.setUpdateUser("system");

        // Upsert: UPDATE first, then INSERT
        jdbcTemplate.update(
                "UPDATE audit_export_task SET query_json=?, format=?, mask_enabled=?, include_diff=?, " +
                "include_timeline=?, status=?, file_path=?, error_message=?, created_by=?, " +
                "completed_at=?, update_time=?, update_user=? WHERE id=?",
                entity.getQueryJson(), entity.getFormat(), entity.getMaskEnabled(),
                entity.getIncludeDiff(), entity.getIncludeTimeline(),
                entity.getStatus(), entity.getFilePath(), entity.getErrorMessage(),
                entity.getCreatedBy(), entity.getCompletedAt(),
                entity.getUpdateTime(), entity.getUpdateUser(), entity.getId()
        );

        jdbcTemplate.update(
                "INSERT OR IGNORE INTO audit_export_task (" +
                "id, query_json, format, mask_enabled, include_diff, include_timeline, " +
                "status, file_path, error_message, created_by, created_at, completed_at, " +
                "create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getQueryJson(), entity.getFormat(),
                entity.getMaskEnabled(), entity.getIncludeDiff(), entity.getIncludeTimeline(),
                entity.getStatus(), entity.getFilePath(), entity.getErrorMessage(),
                entity.getCreatedBy(), entity.getCreatedAt(), entity.getCompletedAt(),
                entity.getCreateTime(), entity.getUpdateTime(),
                entity.getCreateUser(), entity.getUpdateUser()
        );

        task.setId(entity.getId());
        return task;
    }

    @Override
    public Optional<ExportTask> findById(String id) {
        List<AuditExportTaskEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_export_task WHERE id = ?", rowMapper, id);
        return results.stream().findFirst().map(ComplianceConverter::toExportTaskDomain);
    }

    @Override
    public List<ExportTask> findHistory(int limit, int offset) {
        List<AuditExportTaskEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_export_task ORDER BY created_at DESC LIMIT ? OFFSET ?",
                rowMapper, limit, offset);
        return entities.stream().map(ComplianceConverter::toExportTaskDomain).toList();
    }

    @Override
    public long countToday() {
        String today = LocalDate.now().toString();
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_export_task WHERE created_at >= ?",
                Long.class, today);
        return result != null ? result : 0;
    }
}