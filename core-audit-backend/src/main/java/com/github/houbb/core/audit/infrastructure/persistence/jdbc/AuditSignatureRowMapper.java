package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSignatureEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * audit_signature 表 RowMapper
 */
public class AuditSignatureRowMapper implements RowMapper<AuditSignatureEntity> {

    @Override
    public AuditSignatureEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditSignatureEntity entity = new AuditSignatureEntity();
        entity.setId(rs.getString("id"));
        entity.setAuditId(rs.getString("audit_id"));
        entity.setHash(rs.getString("hash"));
        entity.setPreviousHash(rs.getString("previous_hash"));
        entity.setAlgorithm(rs.getString("algorithm"));
        entity.setCreatedAt(rs.getString("created_at"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}