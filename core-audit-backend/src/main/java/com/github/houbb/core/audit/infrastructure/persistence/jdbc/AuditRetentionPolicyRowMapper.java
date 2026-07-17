package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditRetentionPolicyEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * audit_retention_policy 表 RowMapper
 */
public class AuditRetentionPolicyRowMapper implements RowMapper<AuditRetentionPolicyEntity> {

    @Override
    public AuditRetentionPolicyEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditRetentionPolicyEntity entity = new AuditRetentionPolicyEntity();
        entity.setId(rs.getString("id"));
        entity.setModule(rs.getString("module"));
        entity.setAction(rs.getString("action"));
        entity.setRetentionDays(rs.getString("retention_days"));
        entity.setArchive(rs.getString("archive"));
        entity.setEnabled(rs.getString("enabled"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}