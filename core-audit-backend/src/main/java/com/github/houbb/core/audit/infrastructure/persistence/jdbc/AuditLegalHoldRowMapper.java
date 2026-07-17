package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditLegalHoldEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * audit_legal_hold 表 RowMapper
 */
public class AuditLegalHoldRowMapper implements RowMapper<AuditLegalHoldEntity> {

    @Override
    public AuditLegalHoldEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditLegalHoldEntity entity = new AuditLegalHoldEntity();
        entity.setId(rs.getString("id"));
        entity.setAuditId(rs.getString("audit_id"));
        entity.setReason(rs.getString("reason"));
        entity.setOwner(rs.getString("owner"));
        entity.setExpiredAt(rs.getString("expired_at"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}