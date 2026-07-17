package com.github.houbb.core.audit.infrastructure.persistence.repository;

import com.github.houbb.core.audit.application.domain.enums.ReplayStepType;
import com.github.houbb.core.audit.application.domain.replay.ReplaySession;
import com.github.houbb.core.audit.application.domain.replay.ReplayStep;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditReplayEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditReplayStepEntity;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditReplayRowMapper;
import com.github.houbb.core.audit.infrastructure.persistence.jdbc.AuditReplayStepRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcReplayRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private JdbcReplayRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        repository = new JdbcReplayRepository(jdbcTemplate);
    }

    private AuditReplayEntity buildReplayEntity(String id, String timelineId) {
        AuditReplayEntity entity = new AuditReplayEntity();
        entity.setId(id);
        entity.setTimelineId(timelineId);
        entity.setTitle("Test Replay");
        entity.setDuration("5000");
        entity.setCreatedAt(LocalDateTime.now().toString());
        return entity;
    }

    private AuditReplayStepEntity buildStepEntity(String id, String replayId, int sequence, String stepType) {
        AuditReplayStepEntity entity = new AuditReplayStepEntity();
        entity.setId(id);
        entity.setReplayId(replayId);
        entity.setSequence(String.valueOf(sequence));
        entity.setStepType(stepType);
        entity.setTitle("Step " + sequence);
        entity.setPayloadJson("{\"key\":\"value\"}");
        return entity;
    }

    @Test
    @DisplayName("save — inserts new replay + steps")
    void shouldSaveNewReplay() {
        ReplaySession session = ReplaySession.builder()
                .id("rpl-save")
                .timelineId("tl-save")
                .title("Save Test")
                .duration(3000L)
                .steps(List.of(
                        ReplayStep.of(1, ReplayStepType.AUDIT, "Audit step"),
                        ReplayStep.of(2, ReplayStepType.FINISH, "Finish")
                ))
                .createdAt(LocalDateTime.now())
                .build();

        when(jdbcTemplate.update(contains("UPDATE audit_replay"), any(Object[].class)))
                .thenReturn(1);
        when(jdbcTemplate.update(contains("INSERT OR IGNORE INTO audit_replay"), any(Object[].class)))
                .thenReturn(1);
        when(jdbcTemplate.update(contains("DELETE FROM audit_replay_step"), any(Object[].class)))
                .thenReturn(2);
        when(jdbcTemplate.update(contains("INSERT INTO audit_replay_step"), any(Object[].class)))
                .thenReturn(1);

        ReplaySession result = repository.save(session);

        assertNotNull(result);
        assertEquals("rpl-save", result.getId());
        // Expected calls: UPDATE audit_replay, INSERT OR IGNORE audit_replay, DELETE steps, + 2 INSERT steps
        verify(jdbcTemplate, atLeast(4)).update(anyString(), any(Object[].class));
    }

    @Test
    @DisplayName("findById — returns complete ReplaySession with steps")
    void shouldFindReplayById() {
        String replayId = "rpl-find";
        AuditReplayEntity entity = buildReplayEntity(replayId, "tl-find");
        AuditReplayStepEntity step1 = buildStepEntity("stp-1", replayId, 1, "REQUEST");
        AuditReplayStepEntity step2 = buildStepEntity("stp-2", replayId, 2, "AUDIT");

        when(jdbcTemplate.query(eq("SELECT * FROM audit_replay WHERE id = ?"), any(AuditReplayRowMapper.class), eq(replayId)))
                .thenReturn(List.of(entity));
        when(jdbcTemplate.query(eq("SELECT * FROM audit_replay_step WHERE replay_id = ? ORDER BY sequence ASC"), any(AuditReplayStepRowMapper.class), eq(replayId)))
                .thenReturn(List.of(step1, step2));

        Optional<ReplaySession> result = repository.findById(replayId);

        assertTrue(result.isPresent());
        assertEquals(replayId, result.get().getId());
        assertEquals("tl-find", result.get().getTimelineId());
        assertEquals(2, result.get().getSteps().size());
    }

    @Test
    @DisplayName("findById — returns empty when not found")
    void shouldReturnEmptyWhenReplayNotFound() {
        when(jdbcTemplate.query(eq("SELECT * FROM audit_replay WHERE id = ?"), any(AuditReplayRowMapper.class), eq("missing")))
                .thenReturn(List.of());

        Optional<ReplaySession> result = repository.findById("missing");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByTimelineId — returns ReplaySession by timeline_id")
    void shouldFindReplayByTimelineId() {
        String timelineId = "tl-by-timeline";
        AuditReplayEntity entity = buildReplayEntity("rpl-by-tl", timelineId);
        AuditReplayStepEntity step = buildStepEntity("stp-1", "rpl-by-tl", 1, "EVENT");

        when(jdbcTemplate.query(eq("SELECT * FROM audit_replay WHERE timeline_id = ?"), any(AuditReplayRowMapper.class), eq(timelineId)))
                .thenReturn(List.of(entity));
        when(jdbcTemplate.query(eq("SELECT * FROM audit_replay_step WHERE replay_id = ? ORDER BY sequence ASC"), any(AuditReplayStepRowMapper.class), eq("rpl-by-tl")))
                .thenReturn(List.of(step));

        Optional<ReplaySession> result = repository.findByTimelineId(timelineId);

        assertTrue(result.isPresent());
        assertEquals(timelineId, result.get().getTimelineId());
        assertEquals(1, result.get().getSteps().size());
    }

    @Test
    @DisplayName("findByTimelineId — returns empty when not found")
    void shouldReturnEmptyWhenTimelineNotReplayed() {
        when(jdbcTemplate.query(eq("SELECT * FROM audit_replay WHERE timeline_id = ?"), any(AuditReplayRowMapper.class), eq("no-replay")))
                .thenReturn(List.of());

        Optional<ReplaySession> result = repository.findByTimelineId("no-replay");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("countToday — returns today's replay count")
    void shouldCountToday() {
        when(jdbcTemplate.queryForObject(contains("SELECT COUNT(*) FROM audit_replay WHERE create_time >="), eq(Long.class), anyString()))
                .thenReturn(10L);

        long count = repository.countToday();
        assertEquals(10L, count);
    }

    @Test
    @DisplayName("avgStepsToday — returns average steps")
    void shouldAvgStepsToday() {
        when(jdbcTemplate.queryForObject(contains("AVG(cnt)"), eq(Double.class), anyString()))
                .thenReturn(7.5);

        double avg = repository.avgStepsToday();
        assertEquals(7.5, avg, 0.01);
    }

    @Test
    @DisplayName("maxStepsToday — returns max steps")
    void shouldMaxStepsToday() {
        when(jdbcTemplate.queryForObject(contains("MAX(cnt)"), eq(Integer.class), anyString()))
                .thenReturn(35);

        int max = repository.maxStepsToday();
        assertEquals(35, max);
    }

    @Test
    @DisplayName("avgDurationToday — returns average duration")
    void shouldAvgDurationToday() {
        when(jdbcTemplate.queryForObject(contains("AVG(duration)"), eq(Double.class), anyString()))
                .thenReturn(2800.0);

        double avg = repository.avgDurationToday();
        assertEquals(2800.0, avg, 0.01);
    }
}