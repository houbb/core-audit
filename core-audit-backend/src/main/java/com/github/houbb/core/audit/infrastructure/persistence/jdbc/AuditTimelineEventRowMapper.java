package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditTimelineEventEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 时间线事件关联行映射器
 * <p>将 audit_timeline_event 表查询结果映射为 AuditTimelineEventEntity。</p>
 */
public class AuditTimelineEventRowMapper implements RowMapper<AuditTimelineEventEntity> {

    @Override
    public AuditTimelineEventEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditTimelineEventEntity entity = new AuditTimelineEventEntity();
        entity.setId(rs.getString("id"));
        entity.setTimelineId(rs.getString("timeline_id"));
        entity.setAuditId(rs.getString("audit_id"));
        entity.setSequence(rs.getString("sequence"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}
