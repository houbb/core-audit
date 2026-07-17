package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.intelligence.AuditInsight;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;
import com.github.houbb.core.audit.application.port.AuditInsightRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.IntelligenceConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditInsightEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditInsightRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC 实现 AuditInsightRepository
 */
@Repository
public class JdbcAuditInsightRepository implements AuditInsightRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditInsightRowMapper rowMapper = new AuditInsightRowMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public JdbcAuditInsightRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuditInsight save(AuditInsight insight) {
        AuditInsightEntity entity = IntelligenceConverter.toInsightEntity(insight);
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        String now = LocalDateTime.now().format(FORMATTER);
        entity.setCreateTime(entity.getCreateTime() != null ? entity.getCreateTime() : now);
        entity.setUpdateTime(now);
        entity.setCreateUser(entity.getCreateUser() != null ? entity.getCreateUser() : "system");
        entity.setUpdateUser("system");

        jdbcTemplate.update(
                "UPDATE audit_insight SET audit_id=?, title=?, severity=?, summary=?, suggestion=?, " +
                "evidence_json=?, agent_name=?, update_time=?, update_user=? WHERE id=?",
                entity.getAuditId(), entity.getTitle(), entity.getSeverity(),
                entity.getSummary(), entity.getSuggestion(), entity.getEvidenceJson(),
                entity.getAgentName(), entity.getUpdateTime(), entity.getUpdateUser(), entity.getId()
        );

        jdbcTemplate.update(
                "INSERT OR IGNORE INTO audit_insight (" +
                "id, audit_id, title, severity, summary, suggestion, " +
                "evidence_json, agent_name, create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getAuditId(), entity.getTitle(), entity.getSeverity(),
                entity.getSummary(), entity.getSuggestion(), entity.getEvidenceJson(),
                entity.getAgentName(), entity.getCreateTime(), entity.getUpdateTime(),
                entity.getCreateUser(), entity.getUpdateUser()
        );

        insight.setId(entity.getId());
        return insight;
    }

    @Override
    public Optional<AuditInsight> findById(String id) {
        List<AuditInsightEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_insight WHERE id = ?", rowMapper, id);
        return results.stream().findFirst().map(IntelligenceConverter::toInsightDomain);
    }

    @Override
    public List<AuditInsight> findByAuditId(String auditId) {
        List<AuditInsightEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_insight WHERE audit_id = ? ORDER BY create_time DESC", rowMapper, auditId);
        return entities.stream().map(IntelligenceConverter::toInsightDomain).toList();
    }

    @Override
    public List<AuditInsight> findAll(int page, int size) {
        int offset = (page - 1) * size;
        List<AuditInsightEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_insight ORDER BY create_time DESC LIMIT ? OFFSET ?",
                rowMapper, size, offset);
        return entities.stream().map(IntelligenceConverter::toInsightDomain).toList();
    }

    @Override
    public List<AuditInsight> findBySeverity(RiskLevel severity, int page, int size) {
        int offset = (page - 1) * size;
        List<AuditInsightEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_insight WHERE severity = ? ORDER BY create_time DESC LIMIT ? OFFSET ?",
                rowMapper, severity.name(), size, offset);
        return entities.stream().map(IntelligenceConverter::toInsightDomain).toList();
    }

    @Override
    public long countToday() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_insight WHERE create_time LIKE ?",
                Long.class, today + "%");
        return result != null ? result : 0;
    }

    @Override
    public List<AuditInsight> findGlobalRecommendations(int limit) {
        List<AuditInsightEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_insight WHERE audit_id IS NULL ORDER BY create_time DESC LIMIT ?",
                rowMapper, limit);
        return entities.stream().map(IntelligenceConverter::toInsightDomain).toList();
    }
}