package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.AuditEventPage;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.port.AuditEventRepository;
import com.github.houbb.core.audit.application.query.AuditEventQuery;
import com.github.houbb.core.audit.infrastructure.persistence.converter.AuditEventConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditEventEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditEventRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * JDBC 实现 AuditEventRepository
 * <p>使用 JdbcTemplate，SQL 完全可控。</p>
 */
@Repository
public class JdbcAuditEventRepository implements AuditEventRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditEventRowMapper rowMapper = new AuditEventRowMapper();

    public JdbcAuditEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        AuditEventEntity entity = AuditEventConverter.toEntity(event);

        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateUser("system");
        entity.setUpdateUser("system");

        jdbcTemplate.update(
                "INSERT INTO audit_event (" +
                "id, module, action, target_type, target_id, operator_id, operator_name, " +
                "result, description, client_ip, request_uri, request_method, trace_id, " +
                "metadata, created_at, " +
                "event_id, event_type, source, version, occurred_at, published, publish_time, publish_result, " +
                "create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                entity.getId(),
                entity.getModule(),
                entity.getAction(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getOperatorId(),
                entity.getOperatorName(),
                entity.getResult(),
                entity.getDescription(),
                entity.getClientIp(),
                entity.getRequestUri(),
                entity.getRequestMethod(),
                entity.getTraceId(),
                entity.getMetadata(),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : now,
                entity.getEventId(),
                entity.getEventType(),
                entity.getSource(),
                entity.getVersion(),
                entity.getOccurredAt(),
                entity.getPublished() != null ? entity.getPublished() : 1,
                entity.getPublishTime(),
                entity.getPublishResult(),
                entity.getCreateTime(),
                entity.getUpdateTime(),
                entity.getCreateUser(),
                entity.getUpdateUser()
        );

        return findById(entity.getId()).orElse(event);
    }

    @Override
    public Optional<AuditEvent> findById(String id) {
        List<AuditEventEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_event WHERE id = ?",
                rowMapper,
                id
        );
        return results.stream()
                .findFirst()
                .map(AuditEventConverter::toDomain);
    }

    @Override
    public AuditEventPage findAll(AuditEventQuery query) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_event WHERE 1=1");
        List<Object> params = new ArrayList<>();

        appendFilters(sql, params, query);

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(query.getSize());
        params.add((query.getPage() - 1) * query.getSize());

        List<AuditEvent> items = jdbcTemplate.query(sql.toString(), rowMapper, params.toArray())
                .stream()
                .map(AuditEventConverter::toDomain)
                .toList();

        long total = countByFilter(query);

        return new AuditEventPage(items, query.getPage(), query.getSize(), total);
    }

    @Override
    public long countByFilter(AuditEventQuery query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_event WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, query);

        Long result = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return result != null ? result : 0;
    }

    @Override
    public long countToday() {
        String today = LocalDate.now().toString();
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_event WHERE created_at >= ?",
                Long.class,
                today
        );
        return result != null ? result : 0;
    }

    @Override
    public long countTodaySuccess() {
        String today = LocalDate.now().toString();
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_event WHERE created_at >= ? AND result = 'SUCCESS'",
                Long.class,
                today
        );
        return result != null ? result : 0;
    }

    @Override
    public long countTodayFail() {
        String today = LocalDate.now().toString();
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_event WHERE created_at >= ? AND result = 'FAIL'",
                Long.class,
                today
        );
        return result != null ? result : 0;
    }

    @Override
    public int countActiveModulesToday() {
        String today = LocalDate.now().toString();
        Integer result = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT module) FROM audit_event WHERE created_at >= ?",
                Integer.class,
                today
        );
        return result != null ? result : 0;
    }

    @Override
    public long countTodayPublished() {
        String today = LocalDate.now().toString();
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_event WHERE created_at >= ? AND published = 1",
                Long.class,
                today
        );
        return result != null ? result : 0;
    }

    @Override
    public long countTodayPublishFailed() {
        String today = LocalDate.now().toString();
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_event WHERE created_at >= ? AND publish_result = 'FAIL'",
                Long.class,
                today
        );
        return result != null ? result : 0;
    }

    // ======== private helpers ========

    private void appendFilters(StringBuilder sql, List<Object> params, AuditEventQuery query) {
        if (query.getModule() != null) {
            sql.append(" AND module = ?");
            params.add(query.getModule().name());
        }
        if (query.getAction() != null) {
            sql.append(" AND action = ?");
            params.add(query.getAction().name());
        }
        if (query.getEventType() != null) {
            sql.append(" AND event_type = ?");
            params.add(query.getEventType().name());
        }
        if (query.getResult() != null) {
            sql.append(" AND result = ?");
            params.add(query.getResult().name());
        }
        if (query.getOperator() != null && !query.getOperator().isBlank()) {
            sql.append(" AND operator_name LIKE ?");
            params.add("%" + query.getOperator() + "%");
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            sql.append(" AND (description LIKE ? OR target_id LIKE ? OR operator_name LIKE ?)");
            String kw = "%" + query.getKeyword() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (query.getStartTime() != null) {
            sql.append(" AND created_at >= ?");
            params.add(query.getStartTime().toString());
        }
        if (query.getEndTime() != null) {
            sql.append(" AND created_at <= ?");
            params.add(query.getEndTime().toString());
        }
    }
}