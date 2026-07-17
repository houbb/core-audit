package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditInsightEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AuditInsightEntity RowMapper
 */
public class AuditInsightRowMapper implements RowMapper<AuditInsightEntity> {

    @Override
    public AuditInsightEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditInsightEntity entity = new AuditInsightEntity();
        entity.setId(rs.getString("id"));
        entity.setAuditId(rs.getString("audit_id"));
        entity.setTitle(rs.getString("title"));
        entity.setSeverity(rs.getString("severity"));
        entity.setSummary(rs.getString("summary"));
        entity.setSuggestion(rs.getString("suggestion"));
        entity.setEvidenceJson(rs.getString("evidence_json"));
        entity.setAgentName(rs.getString("agent_name"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}