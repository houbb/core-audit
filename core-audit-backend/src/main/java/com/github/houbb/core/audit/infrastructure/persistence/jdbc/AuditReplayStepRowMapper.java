package com.github.houbb.core.audit.infrastructure.persistence.jdbc;

import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditReplayStepEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper: ResultSet → AuditReplayStepEntity
 */
public class AuditReplayStepRowMapper implements RowMapper<AuditReplayStepEntity> {

    @Override
    public AuditReplayStepEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditReplayStepEntity entity = new AuditReplayStepEntity();
        entity.setId(rs.getString("id"));
        entity.setReplayId(rs.getString("replay_id"));
        entity.setSequence(rs.getString("sequence"));
        entity.setStepType(rs.getString("step_type"));
        entity.setTitle(rs.getString("title"));
        entity.setPayloadJson(rs.getString("payload_json"));
        entity.setCreateTime(rs.getString("create_time"));
        entity.setUpdateTime(rs.getString("update_time"));
        entity.setCreateUser(rs.getString("create_user"));
        entity.setUpdateUser(rs.getString("update_user"));
        return entity;
    }
}