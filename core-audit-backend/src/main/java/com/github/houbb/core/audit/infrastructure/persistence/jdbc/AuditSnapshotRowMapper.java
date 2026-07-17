package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSnapshotEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper: ResultSet → AuditSnapshotEntity
 */
public class AuditSnapshotRowMapper implements RowMapper<AuditSnapshotEntity> {

    @Override
    public AuditSnapshotEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditSnapshotEntity entity = new AuditSnapshotEntity();
        entity.setId(rs.getString("id"));
        entity.setAuditId(rs.getString("audit_id"));
        entity.setSnapshotType(rs.getString("snapshot_type"));
        entity.setContentJson(rs.getString("content_json"));
        entity.setCreatedAt(rs.getString("created_at"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}
