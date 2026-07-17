package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.replay.ReplaySession;
import com.github.houbb.core.audit.application.port.ReplayRepository;
import com.github.houbb.core.audit.infrastructure.persistence.converter.ReplayConverter;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditReplayEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditReplayStepEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditReplayRowMapper;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditReplayStepRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC 实现 ReplayRepository
 * <p>使用 JdbcTemplate，支持 SQLite / MySQL 双方言。</p>
 */
@Repository
public class JdbcReplayRepository implements ReplayRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditReplayRowMapper replayRowMapper = new AuditReplayRowMapper();
    private final AuditReplayStepRowMapper stepRowMapper = new AuditReplayStepRowMapper();
    private final boolean sqlite;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public JdbcReplayRepository(JdbcTemplate jdbcTemplate) {
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
    public ReplaySession save(ReplaySession session) {
        String now = LocalDateTime.now().format(FORMATTER);
        AuditReplayEntity entity = ReplayConverter.toReplayEntity(session);
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(UUID.randomUUID().toString());
        }
        entity.setCreateTime(entity.getCreateTime() != null ? entity.getCreateTime() : now);
        entity.setUpdateTime(now);
        entity.setCreateUser(entity.getCreateUser() != null ? entity.getCreateUser() : "system");
        entity.setUpdateUser("system");

        // Upsert: update first, then insert if not found
        jdbcTemplate.update(
                "UPDATE audit_replay SET title=?, duration=?, created_at=?, update_time=?, update_user=? WHERE id=?",
                entity.getTitle(), entity.getDuration(), entity.getCreatedAt(),
                entity.getUpdateTime(), entity.getUpdateUser(), entity.getId()
        );

        jdbcTemplate.update(
                "INSERT OR IGNORE INTO audit_replay (" +
                "id, timeline_id, title, duration, created_at, " +
                "create_time, update_time, create_user, update_user" +
                ") VALUES (?,?,?,?,?,?,?,?,?)",
                entity.getId(), entity.getTimelineId(), entity.getTitle(), entity.getDuration(),
                entity.getCreatedAt(), entity.getCreateTime(), entity.getUpdateTime(),
                entity.getCreateUser(), entity.getUpdateUser()
        );

        session.setId(entity.getId());

        // Save steps: delete existing, then batch insert
        jdbcTemplate.update("DELETE FROM audit_replay_step WHERE replay_id = ?", entity.getId());

        List<AuditReplayStepEntity> stepEntities = ReplayConverter.toStepEntities(session);
        for (AuditReplayStepEntity stepEntity : stepEntities) {
            if (stepEntity.getId() == null || stepEntity.getId().isBlank()) {
                stepEntity.setId(UUID.randomUUID().toString());
            }
            stepEntity.setCreateTime(now);
            stepEntity.setUpdateTime(now);
            stepEntity.setCreateUser("system");
            stepEntity.setUpdateUser("system");

            jdbcTemplate.update(
                    "INSERT INTO audit_replay_step (" +
                    "id, replay_id, sequence, step_type, title, payload_json, " +
                    "create_time, update_time, create_user, update_user" +
                    ") VALUES (?,?,?,?,?,?,?,?,?,?)",
                    stepEntity.getId(), stepEntity.getReplayId(), stepEntity.getSequence(),
                    stepEntity.getStepType(), stepEntity.getTitle(), stepEntity.getPayloadJson(),
                    stepEntity.getCreateTime(), stepEntity.getUpdateTime(),
                    stepEntity.getCreateUser(), stepEntity.getUpdateUser()
            );
        }

        return session;
    }

    @Override
    public Optional<ReplaySession> findById(String id) {
        List<AuditReplayEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_replay WHERE id = ?",
                replayRowMapper, id
        );
        if (entities.isEmpty()) return Optional.empty();

        AuditReplayEntity entity = entities.get(0);
        List<AuditReplayStepEntity> stepEntities = jdbcTemplate.query(
                "SELECT * FROM audit_replay_step WHERE replay_id = ? ORDER BY sequence ASC",
                stepRowMapper, id
        );
        return Optional.of(ReplayConverter.toDomain(entity, stepEntities));
    }

    @Override
    public Optional<ReplaySession> findByTimelineId(String timelineId) {
        List<AuditReplayEntity> entities = jdbcTemplate.query(
                "SELECT * FROM audit_replay WHERE timeline_id = ?",
                replayRowMapper, timelineId
        );
        if (entities.isEmpty()) return Optional.empty();

        AuditReplayEntity entity = entities.get(0);
        List<AuditReplayStepEntity> stepEntities = jdbcTemplate.query(
                "SELECT * FROM audit_replay_step WHERE replay_id = ? ORDER BY sequence ASC",
                stepRowMapper, entity.getId()
        );
        return Optional.of(ReplayConverter.toDomain(entity, stepEntities));
    }

    // ======== Dashboard ========

    @Override
    public long countToday() {
        String today = LocalDate.now().toString();
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_replay WHERE create_time >= ?",
                Long.class, today
        );
        return result != null ? result : 0;
    }

    @Override
    public double avgStepsToday() {
        String today = LocalDate.now().toString();
        Double result = jdbcTemplate.queryForObject(
                "SELECT AVG(cnt) FROM (" +
                "  SELECT COUNT(*) as cnt FROM audit_replay_step rs " +
                "  JOIN audit_replay r ON rs.replay_id = r.id " +
                "  WHERE r.create_time >= ? GROUP BY rs.replay_id" +
                ")",
                Double.class, today
        );
        return result != null ? result : 0.0;
    }

    @Override
    public int maxStepsToday() {
        String today = LocalDate.now().toString();
        Integer result = jdbcTemplate.queryForObject(
                "SELECT MAX(cnt) FROM (" +
                "  SELECT COUNT(*) as cnt FROM audit_replay_step rs " +
                "  JOIN audit_replay r ON rs.replay_id = r.id " +
                "  WHERE r.create_time >= ? GROUP BY rs.replay_id" +
                ")",
                Integer.class, today
        );
        return result != null ? result : 0;
    }

    @Override
    public double avgDurationToday() {
        String today = LocalDate.now().toString();
        Double result = jdbcTemplate.queryForObject(
                "SELECT AVG(duration) FROM audit_replay WHERE create_time >= ? AND duration IS NOT NULL",
                Double.class, today
        );
        return result != null ? result : 0.0;
    }
}