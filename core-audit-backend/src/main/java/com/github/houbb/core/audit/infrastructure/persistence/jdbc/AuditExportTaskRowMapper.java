package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditExportTaskEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * audit_export_task 表 RowMapper
 */
public class AuditExportTaskRowMapper implements RowMapper<AuditExportTaskEntity> {

    @Override
    public AuditExportTaskEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditExportTaskEntity entity = new AuditExportTaskEntity();
        entity.setId(rs.getString("id"));
        entity.setQueryJson(rs.getString("query_json"));
        entity.setFormat(rs.getString("format"));
        entity.setMaskEnabled(rs.getString("mask_enabled"));
        entity.setIncludeDiff(rs.getString("include_diff"));
        entity.setIncludeTimeline(rs.getString("include_timeline"));
        entity.setStatus(rs.getString("status"));
        entity.setFilePath(rs.getString("file_path"));
        entity.setErrorMessage(rs.getString("error_message"));
        entity.setCreatedBy(rs.getString("created_by"));
        entity.setCreatedAt(rs.getString("created_at"));
        entity.setCompletedAt(rs.getString("completed_at"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}