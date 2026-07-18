package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditEventEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper: ResultSet → AuditEventEntity
 */
public class AuditEventRowMapper implements RowMapper<AuditEventEntity> {

    @Override
    public AuditEventEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditEventEntity entity = new AuditEventEntity();
        entity.setId(rs.getString("id"));
        entity.setModule(rs.getString("module"));
        entity.setAction(rs.getString("action"));
        entity.setTargetType(rs.getString("target_type"));
        entity.setTargetId(rs.getString("target_id"));
        entity.setOperatorId(rs.getString("operator_id"));
        entity.setOperatorName(rs.getString("operator_name"));
        entity.setResult(rs.getString("result"));
        entity.setDescription(rs.getString("description"));
        entity.setClientIp(rs.getString("client_ip"));
        entity.setRequestUri(rs.getString("request_uri"));
        entity.setRequestMethod(rs.getString("request_method"));
        entity.setTraceId(rs.getString("trace_id"));
        entity.setMetadata(rs.getString("metadata"));
        entity.setCreatedAt(rs.getString("created_at"));
        // P1 fields
        entity.setEventId(rs.getString("event_id"));
        entity.setEventType(rs.getString("event_type"));
        entity.setSource(rs.getString("source"));
        entity.setVersion(rs.getString("version"));
        entity.setOccurredAt(rs.getString("occurred_at"));
        int pub = rs.getInt("published");
        entity.setPublished(rs.wasNull() ? null : pub);
        entity.setPublishTime(rs.getString("publish_time"));
        entity.setPublishResult(rs.getString("publish_result"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        // P2 context
        entity.setContextJson(rs.getString("context_json"));
        // P9 tenant
        entity.setTenant(rs.getString("tenant"));
        return entity;
    }
}