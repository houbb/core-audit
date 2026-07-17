package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditRiskEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AuditRiskEntity RowMapper
 */
public class AuditRiskRowMapper implements RowMapper<AuditRiskEntity> {

    @Override
    public AuditRiskEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditRiskEntity entity = new AuditRiskEntity();
        entity.setId(rs.getString("id"));
        entity.setAuditId(rs.getString("audit_id"));
        entity.setRiskScore(rs.getInt("risk_score"));
        entity.setRiskLevel(rs.getString("risk_level"));
        entity.setReason(rs.getString("reason"));
        entity.setRuleName(rs.getString("rule_name"));
        entity.setAiAnalysis(rs.getString("ai_analysis"));
        entity.setAnalyzedAt(rs.getString("analyzed_at"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}