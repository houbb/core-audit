package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.compliance.LegalHold;
import com.github.houbb.core.audit.application.port.LegalHoldRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.ComplianceConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditLegalHoldEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditLegalHoldRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC 实现 LegalHoldRepository
 */
@Repository
public class JdbcLegalHoldRepository implements LegalHoldRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditLegalHoldRowMapper rowMapper = new AuditLegalHoldRowMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public JdbcLegalHoldRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LegalHold save(LegalHold hold) {
        AuditLegalHoldEntity entity = ComplianceConverter.toLegalHoldEntity(hold);
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        String now = LocalDateTime.now().format(FORMATTER);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateUser("system");
        entity.setUpdateUser("system");

        jdbcTemplate.update(
                "INSERT INTO audit_legal_hold (" +
                "id, audit_id, reason, owner, expired_at, " +
                "create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getAuditId(), entity.getReason(), entity.getOwner(),
                entity.getExpiredAt(),
                entity.getCreateTime(), entity.getUpdateTime(),
                entity.getCreateUser(), entity.getUpdateUser()
        );

        hold.setId(entity.getId());
        return hold;
    }

    @Override
    public Optional<LegalHold> findById(String id) {
        List<AuditLegalHoldEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_legal_hold WHERE id = ?", rowMapper, id);
        return results.stream().findFirst().map(ComplianceConverter::toLegalHoldDomain);
    }

    @Override
    public List<LegalHold> findByAuditId(String auditId) {
        List<AuditLegalHoldEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_legal_hold WHERE audit_id = ?", rowMapper, auditId);
        return entities.stream().map(ComplianceConverter::toLegalHoldDomain).toList();
    }

    @Override
    public List<LegalHold> findAll() {
        List<AuditLegalHoldEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_legal_hold ORDER BY create_time DESC", rowMapper);
        return entities.stream().map(ComplianceConverter::toLegalHoldDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        jdbcTemplate.update("DELETE FROM audit_legal_hold WHERE id = ?", id);
    }

    @Override
    public boolean existsActiveByAuditId(String auditId) {
        // Active = expired_at is NULL (永久) OR expired_at > now
        String now = LocalDateTime.now().format(FORMATTER);
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_legal_hold WHERE audit_id = ? AND (expired_at IS NULL OR expired_at > ?)",
                Long.class, auditId, now);
        return result != null && result > 0;
    }

    @Override
    public long countActive() {
        String now = LocalDateTime.now().format(FORMATTER);
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_legal_hold WHERE expired_at IS NULL OR expired_at > ?",
                Long.class, now);
        return result != null ? result : 0;
    }
}