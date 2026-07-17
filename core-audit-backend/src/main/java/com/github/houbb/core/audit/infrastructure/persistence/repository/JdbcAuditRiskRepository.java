package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.intelligence.AuditRisk;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;
import com.github.houbb.core.audit.application.port.AuditRiskRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.IntelligenceConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditRiskEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditRiskRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC 实现 AuditRiskRepository
 */
@Repository
public class JdbcAuditRiskRepository implements AuditRiskRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditRiskRowMapper rowMapper = new AuditRiskRowMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public JdbcAuditRiskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuditRisk save(AuditRisk risk) {
        AuditRiskEntity entity = IntelligenceConverter.toRiskEntity(risk);
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        String now = LocalDateTime.now().format(FORMATTER);
        entity.setCreateTime(entity.getCreateTime() != null ? entity.getCreateTime() : now);
        entity.setUpdateTime(now);
        entity.setCreateUser(entity.getCreateUser() != null ? entity.getCreateUser() : "system");
        entity.setUpdateUser("system");

        jdbcTemplate.update(
                "UPDATE audit_risk SET audit_id=?, risk_score=?, risk_level=?, reason=?, rule_name=?, " +
                "ai_analysis=?, analyzed_at=?, update_time=?, update_user=? WHERE id=?",
                entity.getAuditId(), entity.getRiskScore(), entity.getRiskLevel(),
                entity.getReason(), entity.getRuleName(), entity.getAiAnalysis(),
                entity.getAnalyzedAt(), entity.getUpdateTime(), entity.getUpdateUser(), entity.getId()
        );

        jdbcTemplate.update(
                "INSERT OR IGNORE INTO audit_risk (" +
                "id, audit_id, risk_score, risk_level, reason, rule_name, " +
                "ai_analysis, analyzed_at, create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getAuditId(), entity.getRiskScore(), entity.getRiskLevel(),
                entity.getReason(), entity.getRuleName(), entity.getAiAnalysis(),
                entity.getAnalyzedAt(), entity.getCreateTime(), entity.getUpdateTime(),
                entity.getCreateUser(), entity.getUpdateUser()
        );

        risk.setId(entity.getId());
        return risk;
    }

    @Override
    public Optional<AuditRisk> findById(String id) {
        List<AuditRiskEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_risk WHERE id = ?", rowMapper, id);
        return results.stream().findFirst().map(IntelligenceConverter::toRiskDomain);
    }

    @Override
    public Optional<AuditRisk> findByAuditId(String auditId) {
        List<AuditRiskEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_risk WHERE audit_id = ? LIMIT 1", rowMapper, auditId);
        return results.stream().findFirst().map(IntelligenceConverter::toRiskDomain);
    }

    @Override
    public List<AuditRisk> findAll(int page, int size) {
        int offset = (page - 1) * size;
        List<AuditRiskEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_risk ORDER BY risk_score DESC LIMIT ? OFFSET ?",
                rowMapper, size, offset);
        return entities.stream().map(IntelligenceConverter::toRiskDomain).toList();
    }

    @Override
    public List<AuditRisk> findByLevel(RiskLevel level, int page, int size) {
        int offset = (page - 1) * size;
        List<AuditRiskEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_risk WHERE risk_level = ? ORDER BY risk_score DESC LIMIT ? OFFSET ?",
                rowMapper, level.name(), size, offset);
        return entities.stream().map(IntelligenceConverter::toRiskDomain).toList();
    }

    @Override
    public long countTodayByLevel(RiskLevel level) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_risk WHERE risk_level = ? AND analyzed_at LIKE ?",
                Long.class, level.name(), today + "%");
        return result != null ? result : 0;
    }

    @Override
    public double avgRiskScoreToday() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Double result = jdbcTemplate.queryForObject(
                "SELECT AVG(risk_score) FROM audit_risk WHERE analyzed_at LIKE ?",
                Double.class, today + "%");
        return result != null ? result : 0.0;
    }

    @Override
    public long countToday() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_risk WHERE analyzed_at LIKE ?",
                Long.class, today + "%");
        return result != null ? result : 0;
    }
}