package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditChangeEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper: ResultSet → AuditChangeEntity
 */
public class AuditChangeRowMapper implements RowMapper<AuditChangeEntity> {

    @Override
    public AuditChangeEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditChangeEntity entity = new AuditChangeEntity();
        entity.setId(rs.getString("id"));
        entity.setAuditId(rs.getString("audit_id"));
        entity.setFieldName(rs.getString("field_name"));
        entity.setChangeType(rs.getString("change_type"));
        entity.setBeforeValue(rs.getString("before_value"));
        entity.setAfterValue(rs.getString("after_value"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}
