package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.compliance.RetentionPolicy;
import com.github.houbb.core.audit.application.port.RetentionPolicyRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.ComplianceConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditRetentionPolicyEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditRetentionPolicyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC 实现 RetentionPolicyRepository
 */
@Repository
public class JdbcRetentionPolicyRepository implements RetentionPolicyRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditRetentionPolicyRowMapper rowMapper = new AuditRetentionPolicyRowMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public JdbcRetentionPolicyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RetentionPolicy save(RetentionPolicy policy) {
        AuditRetentionPolicyEntity entity = ComplianceConverter.toRetentionPolicyEntity(policy);
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        String now = LocalDateTime.now().format(FORMATTER);
        entity.setCreateTime(entity.getCreateTime() != null ? entity.getCreateTime() : now);
        entity.setUpdateTime(now);
        entity.setCreateUser(entity.getCreateUser() != null ? entity.getCreateUser() : "system");
        entity.setUpdateUser("system");

        // Upsert: UPDATE first, then INSERT if not found
        jdbcTemplate.update(
                "UPDATE audit_retention_policy SET module=?, action=?, retention_days=?, archive=?, enabled=?, update_time=?, update_user=? WHERE id=?",
                entity.getModule(), entity.getAction(), entity.getRetentionDays(),
                entity.getArchive(), entity.getEnabled(),
                entity.getUpdateTime(), entity.getUpdateUser(), entity.getId()
        );

        jdbcTemplate.update(
                "INSERT OR IGNORE INTO audit_retention_policy (" +
                "id, module, action, retention_days, archive, enabled, " +
                "create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getModule(), entity.getAction(),
                entity.getRetentionDays(), entity.getArchive(), entity.getEnabled(),
                entity.getCreateTime(), entity.getUpdateTime(),
                entity.getCreateUser(), entity.getUpdateUser()
        );

        policy.setId(entity.getId());
        return policy;
    }

    @Override
    public Optional<RetentionPolicy> findById(String id) {
        List<AuditRetentionPolicyEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_retention_policy WHERE id = ?", rowMapper, id);
        return results.stream().findFirst().map(ComplianceConverter::toRetentionPolicyDomain);
    }

    @Override
    public List<RetentionPolicy> findAll() {
        List<AuditRetentionPolicyEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_retention_policy ORDER BY module, action", rowMapper);
        return entities.stream().map(ComplianceConverter::toRetentionPolicyDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        jdbcTemplate.update("DELETE FROM audit_retention_policy WHERE id = ?", id);
    }

    @Override
    public Optional<RetentionPolicy> findByModuleAndAction(String module, String action) {
        String sql;
        List<AuditRetentionPolicyEntity> results;
        if (action != null && !action.isBlank()) {
            sql = "SELECT * FROM audit_retention_policy WHERE module = ? AND action = ? LIMIT 1";
            results = jdbcTemplate.query(sql, rowMapper, module, action);
        } else {
            sql = "SELECT * FROM audit_retention_policy WHERE module = ? AND action IS NULL LIMIT 1";
            results = jdbcTemplate.query(sql, rowMapper, module);
        }
        return results.stream().findFirst().map(ComplianceConverter::toRetentionPolicyDomain);
    }

    @Override
    public List<RetentionPolicy> findAllEnabled() {
        List<AuditRetentionPolicyEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_retention_policy WHERE enabled = '1' ORDER BY module, action", rowMapper);
        return entities.stream().map(ComplianceConverter::toRetentionPolicyDomain).toList();
    }

    @Override
    public long countEnabled() {
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_retention_policy WHERE enabled = '1'", Long.class);
        return result != null ? result : 0;
    }
}