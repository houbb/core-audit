package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditPatternEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AuditPatternEntity RowMapper
 */
public class AuditPatternRowMapper implements RowMapper<AuditPatternEntity> {

    @Override
    public AuditPatternEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditPatternEntity entity = new AuditPatternEntity();
        entity.setId(rs.getString("id"));
        entity.setType(rs.getString("type"));
        entity.setOwner(rs.getString("owner"));
        entity.setContentJson(rs.getString("content_json"));
        entity.setConfidence(rs.getDouble("confidence"));
        entity.setSampleCount(rs.getInt("sample_count"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}