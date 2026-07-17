package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditReplayEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper: ResultSet → AuditReplayEntity
 */
public class AuditReplayRowMapper implements RowMapper<AuditReplayEntity> {

    @Override
    public AuditReplayEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditReplayEntity entity = new AuditReplayEntity();
        entity.setId(rs.getString("id"));
        entity.setTimelineId(rs.getString("timeline_id"));
        entity.setTitle(rs.getString("title"));
        entity.setDuration(rs.getString("duration"));
        entity.setCreatedAt(rs.getString("created_at"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}