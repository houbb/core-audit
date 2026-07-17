package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.port.ChangeRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.DiffConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditChangeEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditChangeRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * JDBC 实现 ChangeRepository
 */
@Repository
public class JdbcChangeRepository implements ChangeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditChangeRowMapper rowMapper = new AuditChangeRowMapper();

    public JdbcChangeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Change> saveAll(String auditId, List<Change> changes) {
        if (changes == null || changes.isEmpty()) return Collections.emptyList();

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        List<Change> saved = new ArrayList<>();

        for (Change change : changes) {
            AuditChangeEntity entity = DiffConverter.toChangeEntity(change, auditId);
            entity.setId(UUID.randomUUID().toString());
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setCreateUser("system");
            entity.setUpdateUser("system");

            jdbcTemplate.update(
                    "INSERT INTO audit_change (" +
                    "id, audit_id, field_name, change_type, before_value, after_value, " +
                    "create_time, update_time, create_user, update_user" +
                    ") VALUES (?,?,?,?,?,?,?,?,?,?)",
                    entity.getId(),
                    entity.getAuditId(),
                    entity.getFieldName(),
                    entity.getChangeType(),
                    entity.getBeforeValue(),
                    entity.getAfterValue(),
                    entity.getCreateTime(),
                    entity.getUpdateTime(),
                    entity.getCreateUser(),
                    entity.getUpdateUser()
            );
            saved.add(change);
        }
        return saved;
    }

    @Override
    public List<Change> findByAuditId(String auditId) {
        return jdbcTemplate.query(
                "SELECT * FROM audit_change WHERE audit_id = ? ORDER BY field_name ASC",
                rowMapper,
                auditId
        ).stream()
                .map(DiffConverter::toChangeDomain)
                .toList();
    }

    @Override
    public List<Change> search(String fieldName, String afterValue, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_change WHERE change_type != 'UNCHANGED'");
        List<Object> params = new ArrayList<>();

        if (fieldName != null && !fieldName.isBlank()) {
            sql.append(" AND field_name = ?");
            params.add(fieldName);
        }
        if (afterValue != null && !afterValue.isBlank()) {
            sql.append(" AND after_value = ?");
            params.add(afterValue);
        }

        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray())
                .stream()
                .map(DiffConverter::toChangeDomain)
                .toList();
    }

    @Override
    public long countBySearch(String fieldName, String afterValue) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_change WHERE change_type != 'UNCHANGED'");
        List<Object> params = new ArrayList<>();

        if (fieldName != null && !fieldName.isBlank()) {
            sql.append(" AND field_name = ?");
            params.add(fieldName);
        }
        if (afterValue != null && !afterValue.isBlank()) {
            sql.append(" AND after_value = ?");
            params.add(afterValue);
        }

        Long result = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return result != null ? result : 0;
    }

    @Override
    public Map<String, Long> changeTypeDistributionToday() {
        String today = LocalDate.now().toString();
        String sql = "SELECT change_type, COUNT(*) as cnt " +
                "FROM audit_change WHERE create_time >= ? AND change_type != 'UNCHANGED' " +
                "GROUP BY change_type";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, today);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object key = row.get("change_type");
            Object val = row.get("cnt");
            if (key != null) {
                result.put(key.toString(), val instanceof Number ? ((Number) val).longValue() : 0L);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> topChangedFieldsToday(int limit) {
        String today = LocalDate.now().toString();
        String sql = "SELECT field_name, COUNT(*) as cnt " +
                "FROM audit_change WHERE create_time >= ? AND change_type != 'UNCHANGED' " +
                "GROUP BY field_name ORDER BY cnt DESC LIMIT ?";
        return jdbcTemplate.queryForList(sql, today, limit);
    }
}
