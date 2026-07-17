package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.TimelineType;
import com.github.houbb.core.audit.application.domain.timeline.Timeline;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;
import com.github.houbb.core.audit.application.port.TimelineRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.TimelineConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditTimelineEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditTimelineEventEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditEventRowMapper;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditTimelineEventRowMapper;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditTimelineRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * JDBC 实现 TimelineRepository
 * <p>使用 JdbcTemplate，支持 SQLite / MySQL 双方言。</p>
 */
@Repository
public class JdbcTimelineRepository implements TimelineRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditTimelineRowMapper timelineRowMapper = new AuditTimelineRowMapper();
    private final AuditTimelineEventRowMapper eventRowMapper = new AuditTimelineEventRowMapper();
    private final AuditEventRowMapper auditEventRowMapper = new AuditEventRowMapper();
    private final boolean sqlite;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public JdbcTimelineRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlite = isSqlite();
    }

    private boolean isSqlite() {
        try {
            DataSource ds = jdbcTemplate.getDataSource();
            if (ds != null) {
                try (Connection conn = ds.getConnection()) {
                    String url = conn.getMetaData().getURL();
                    return url != null && url.contains("sqlite");
                }
            }
        } catch (SQLException e) {
            // Fallback: assume not SQLite
        }
        return false;
    }

    @Override
    public Timeline save(Timeline timeline) {
        String now = LocalDateTime.now().format(FORMATTER);
        AuditTimelineEntity entity = TimelineConverter.toEntity(timeline);
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        entity.setCreateTime(entity.getCreateTime() != null ? entity.getCreateTime() : now);
        entity.setUpdateTime(now);
        entity.setCreateUser(entity.getCreateUser() != null ? entity.getCreateUser() : "system");
        entity.setUpdateUser("system");

        // Upsert: try update first, insert if not found
        jdbcTemplate.update(
                "UPDATE audit_timeline SET type=?, title=?, start_time=?, end_time=?, " +
                "duration=?, summary=?, update_time=?, update_user=? WHERE id=?",
                entity.getType(), entity.getTitle(), entity.getStartTime(), entity.getEndTime(),
                entity.getDuration(), entity.getSummary(), entity.getUpdateTime(), entity.getUpdateUser(),
                entity.getId()
        );

        // Insert if not existed
        jdbcTemplate.update(
                "INSERT OR IGNORE INTO audit_timeline (" +
                "id, type, title, start_time, end_time, duration, summary, " +
                "create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getType(), entity.getTitle(), entity.getStartTime(),
                entity.getEndTime(), entity.getDuration(), entity.getSummary(),
                entity.getCreateTime(), entity.getUpdateTime(), entity.getCreateUser(), entity.getUpdateUser()
        );

        timeline.setId(entity.getId());
        return timeline;
    }

    @Override
    public Optional<Timeline> findById(String id) {
        AuditTimelineEntity entity = findTimelineEntity(id);
        if (entity == null) return Optional.empty();

        Timeline timeline = TimelineConverter.toDomain(entity);
        // 加载关联的 AuditEvent
        List<AuditEvent> events = loadEventsForTimeline(id);
        timeline.setEvents(events);
        return Optional.of(timeline);
    }

    @Override
    public List<Timeline> findByKey(TimelineKey key) {
        return findTimelinesByTypeAndKey(key.getType(), key.getKey());
    }

    @Override
    public List<Timeline> findByType(TimelineType type, int limit, int offset) {
        List<AuditTimelineEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_timeline WHERE type = ? ORDER BY update_time DESC LIMIT ? OFFSET ?",
                timelineRowMapper,
                type.name(), limit, offset
        );
        return entities.stream().map(TimelineConverter::toDomain).toList();
    }

    @Override
    public List<Timeline> findRecent(int limit) {
        List<AuditTimelineEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_timeline ORDER BY update_time DESC LIMIT ?",
                timelineRowMapper,
                limit
        );
        return entities.stream().map(TimelineConverter::toDomain).toList();
    }

    @Override
    public void appendEvent(String timelineId, String auditId, int sequence) {
        String now = LocalDateTime.now().format(FORMATTER);
        String id = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "INSERT INTO audit_timeline_event (" +
                "id, timeline_id, audit_id, sequence, create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?)",
                id, timelineId, auditId, sequence, now, now, "system", "system"
        );
    }

    @Override
    public int getMaxSequence(String timelineId) {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT MAX(sequence) FROM audit_timeline_event WHERE timeline_id = ?",
                Integer.class, timelineId
        );
        return result != null ? result : 0;
    }

    // ======== Dashboard ========

    @Override
    public long countToday() {
        String today = LocalDate.now().toString();
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_timeline WHERE create_time >= ?",
                Long.class, today
        );
        return result != null ? result : 0;
    }

    @Override
    public double avgLengthToday() {
        String today = LocalDate.now().toString();
        Double result = jdbcTemplate.queryForObject(
                "SELECT AVG(cnt) FROM (" +
                "  SELECT COUNT(*) as cnt FROM audit_timeline_event te " +
                "  JOIN audit_timeline t ON te.timeline_id = t.id " +
                "  WHERE t.create_time >= ? GROUP BY te.timeline_id" +
                ")",
                Double.class, today
        );
        return result != null ? result : 0.0;
    }

    @Override
    public int maxLengthToday() {
        String today = LocalDate.now().toString();
        Integer result = jdbcTemplate.queryForObject(
                "SELECT MAX(cnt) FROM (" +
                "  SELECT COUNT(*) as cnt FROM audit_timeline_event te " +
                "  JOIN audit_timeline t ON te.timeline_id = t.id " +
                "  WHERE t.create_time >= ? GROUP BY te.timeline_id" +
                ")",
                Integer.class, today
        );
        return result != null ? result : 0;
    }

    @Override
    public double avgDurationToday() {
        String today = LocalDate.now().toString();
        Double result = jdbcTemplate.queryForObject(
                "SELECT AVG(duration) FROM audit_timeline WHERE create_time >= ? AND duration IS NOT NULL",
                Double.class, today
        );
        return result != null ? result : 0.0;
    }

    // ======== Private Helpers ========

    private AuditTimelineEntity findTimelineEntity(String id) {
        List<AuditTimelineEntity> results = jdbcTemplate.query(
                "SELECT * FROM audit_timeline WHERE id = ?",
                timelineRowMapper, id
        );
        return results.isEmpty() ? null : results.get(0);
    }

    private List<Timeline> findTimelinesByTypeAndKey(String type, String key) {
        // 根据 TimelineType 的聚合 key 查找
        List<AuditTimelineEntity> entities;
        if ("OBJECT".equals(type)) {
            // ResourceTimelineStrategy: key = "targetType#targetId"
            // 匹配 title 前缀（Object Timeline title 以此为标识）或 id 精确匹配
            entities = jdbcTemplate.query(
                    "SELECT * FROM audit_timeline WHERE type = ? ORDER BY update_time DESC",
                    timelineRowMapper, type
            );
            // 过滤：title 中包含该 key 的
            entities = entities.stream()
                    .filter(e -> e.getTitle() != null && e.getTitle().contains(key))
                    .toList();
        } else {
            // USER / REQUEST / SESSION / WORKFLOW: key 直接是 ID
            entities = jdbcTemplate.query(
                    "SELECT * FROM audit_timeline WHERE type = ? ORDER BY update_time DESC",
                    timelineRowMapper, type
            );
            entities = entities.stream()
                    .filter(e -> e.getTitle() != null && e.getTitle().contains(key))
                    .toList();
        }
        return entities.stream().map(TimelineConverter::toDomain).toList();
    }

    private List<AuditEvent> loadEventsForTimeline(String timelineId) {
        // 通过 audit_timeline_event 联表查询 audit_event，按 sequence 排序
        String sql = "SELECT ae.* FROM audit_event ae " +
                "INNER JOIN audit_timeline_event te ON ae.id = te.audit_id " +
                "WHERE te.timeline_id = ? " +
                "ORDER BY te.sequence ASC";
        return jdbcTemplate.query(sql, auditEventRowMapper, timelineId)
                .stream()
                .map(com.github.houbb.core.audit.infrastructure.persistence.converter.AuditEventConverter::toDomain)
                .toList();
    }
}
