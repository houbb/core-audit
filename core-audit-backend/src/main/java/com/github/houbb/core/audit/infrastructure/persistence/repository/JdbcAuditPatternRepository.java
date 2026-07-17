package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.intelligence.AuditPattern;
import com.github.houbb.core.audit.application.port.AuditPatternRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.IntelligenceConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditPatternEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditPatternRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC 实现 AuditPatternRepository
 */
@Repository
public class JdbcAuditPatternRepository implements AuditPatternRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditPatternRowMapper rowMapper = new AuditPatternRowMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public JdbcAuditPatternRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuditPattern save(AuditPattern pattern) {
        AuditPatternEntity entity = IntelligenceConverter.toPatternEntity(pattern);
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        String now = LocalDateTime.now().format(FORMATTER);
        entity.setCreateTime(entity.getCreateTime() != null ? entity.getCreateTime() : now);
        entity.setUpdateTime(now);
        entity.setCreateUser(entity.getCreateUser() != null ? entity.getCreateUser() : "system");
        entity.setUpdateUser("system");

        jdbcTemplate.update(
                "UPDATE audit_pattern SET type=?, owner=?, content_json=?, confidence=?, sample_count=?, " +
                "update_time=?, update_user=? WHERE id=?",
                entity.getType(), entity.getOwner(), entity.getContentJson(),
                entity.getConfidence(), entity.getSampleCount(),
                entity.getUpdateTime(), entity.getUpdateUser(), entity.getId()
        );

        jdbcTemplate.update(
                "INSERT OR IGNORE INTO audit_pattern (" +
                "id, type, owner, content_json, confidence, sample_count, " +
                "create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getType(), entity.getOwner(), entity.getContentJson(),
                entity.getConfidence(), entity.getSampleCount(),
                entity.getCreateTime(), entity.getUpdateTime(),
                entity.getCreateUser(), entity.getUpdateUser()
        );

        pattern.setId(entity.getId());
        return pattern;
    }

    @Override
    public Optional<AuditPattern> findById(String id) {
        List<AuditPatternEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_pattern WHERE id = ?", rowMapper, id);
        return results.stream().findFirst().map(IntelligenceConverter::toPatternDomain);
    }

    @Override
    public Optional<AuditPattern> findByTypeAndOwner(String type, String owner) {
        List<AuditPatternEntity> results;
        if (owner != null && !owner.isBlank()) {
            results = jdbcTemplate.query(
                    "SELECT * FROM audit_pattern WHERE type = ? AND owner = ? LIMIT 1",
                    rowMapper, type, owner);
        } else {
            results = jdbcTemplate.query(
                    "SELECT * FROM audit_pattern WHERE type = ? AND owner IS NULL LIMIT 1",
                    rowMapper, type);
        }
        return results.stream().findFirst().map(IntelligenceConverter::toPatternDomain);
    }

    @Override
    public List<AuditPattern> findByType(String type) {
        List<AuditPatternEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_pattern WHERE type = ? ORDER BY confidence DESC", rowMapper, type);
        return entities.stream().map(IntelligenceConverter::toPatternDomain).toList();
    }

    @Override
    public List<AuditPattern> findAll() {
        List<AuditPatternEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_pattern ORDER BY type, owner", rowMapper);
        return entities.stream().map(IntelligenceConverter::toPatternDomain).toList();
    }
}