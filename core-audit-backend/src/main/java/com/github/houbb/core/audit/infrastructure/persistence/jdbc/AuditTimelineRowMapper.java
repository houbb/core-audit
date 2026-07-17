package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditTimelineEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 时间线行映射器
 * <p>将 audit_timeline 表查询结果映射为 AuditTimelineEntity。</p>
 */
public class AuditTimelineRowMapper implements RowMapper<AuditTimelineEntity> {

    @Override
    public AuditTimelineEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditTimelineEntity entity = new AuditTimelineEntity();
        entity.setId(rs.getString("id"));
        entity.setType(rs.getString("type"));
        entity.setTitle(rs.getString("title"));
        entity.setStartTime(rs.getString("start_time"));
        entity.setEndTime(rs.getString("end_time"));
        entity.setDuration(rs.getString("duration"));
        entity.setSummary(rs.getString("summary"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}
