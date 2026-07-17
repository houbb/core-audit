package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.compliance.AuditSignature;
import com.github.houbb.core.audit.application.port.AuditSignatureRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.ComplianceConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSignatureEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditSignatureRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC 实现 AuditSignatureRepository
 * <p>签名是不可变的（一旦写入不再修改），因此使用简单 INSERT，不使用 UPSERT。</p>
 */
@Repository
public class JdbcAuditSignatureRepository implements AuditSignatureRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditSignatureRowMapper rowMapper = new AuditSignatureRowMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public JdbcAuditSignatureRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuditSignature save(AuditSignature signature) {
        AuditSignatureEntity entity = ComplianceConverter.toSignatureEntity(signature);
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        String now = LocalDateTime.now().format(FORMATTER);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateUser("system");
        entity.setUpdateUser("system");

        jdbcTemplate.update(
                "INSERT INTO audit_signature (" +
                "id, audit_id, hash, previous_hash, algorithm, created_at, " +
                "create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getAuditId(), entity.getHash(), entity.getPreviousHash(),
                entity.getAlgorithm(), entity.getCreatedAt(),
                entity.getCreateTime(), entity.getUpdateTime(),
                entity.getCreateUser(), entity.getUpdateUser()
        );

        signature.setId(entity.getId());
        return signature;
    }

    @Override
    public Optional<AuditSignature> findByAuditId(String auditId) {
        List<AuditSignatureEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_signature WHERE audit_id = ?", rowMapper, auditId);
        return results.stream().findFirst().map(ComplianceConverter::toSignatureDomain);
    }

    @Override
    public Optional<AuditSignature> findLatest() {
        List<AuditSignatureEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_signature ORDER BY created_at DESC LIMIT 1", rowMapper);
        return results.stream().findFirst().map(ComplianceConverter::toSignatureDomain);
    }

    @Override
    public List<AuditSignature> findAllOrderByCreatedAt(int limit, int offset) {
        List<AuditSignatureEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_signature ORDER BY created_at ASC LIMIT ? OFFSET ?",
                rowMapper, limit, offset);
        return entities.stream().map(ComplianceConverter::toSignatureDomain).toList();
    }

    @Override
    public long count() {
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_signature", Long.class);
        return result != null ? result : 0;
    }

    @Override
    public void deleteByAuditId(String auditId) {
        jdbcTemplate.update("DELETE FROM audit_signature WHERE audit_id = ?", auditId);
    }
}