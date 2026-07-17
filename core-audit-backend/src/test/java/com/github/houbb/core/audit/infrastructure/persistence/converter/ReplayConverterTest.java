package com.github.houbb.core.audit.infrastructure.persistence.converter;

import com.github.houbb.core.audit.application.domain.enums.ReplayStepType;
import com.github.houbb.core.audit.application.domain.replay.ReplaySession;
import com.github.houbb.core.audit.application.domain.replay.ReplayStep;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditReplayEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditReplayStepEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReplayConverterTest {

    @Test
    @DisplayName("toReplayEntity — converts ReplaySession to entity")
    void shouldConvertSessionToEntity() {
        LocalDateTime now = LocalDateTime.now();
        ReplaySession session = ReplaySession.builder()
                .id("rpl-001")
                .timelineId("tl-001")
                .title("Test Replay")
                .duration(5000L)
                .createdAt(now)
                .build();

        AuditReplayEntity entity = ReplayConverter.toReplayEntity(session);

        assertNotNull(entity);
        assertEquals("rpl-001", entity.getId());
        assertEquals("tl-001", entity.getTimelineId());
        assertEquals("Test Replay", entity.getTitle());
        assertEquals("5000", entity.getDuration());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("toReplayEntity — handles null gracefully")
    void shouldHandleNullSession() {
        assertNull(ReplayConverter.toReplayEntity(null));
    }

    @Test
    @DisplayName("toStepEntity — converts ReplayStep to entity")
    void shouldConvertStepToEntity() {
        ReplayStep step = ReplayStep.builder()
                .sequence(1)
                .stepType(ReplayStepType.REQUEST)
                .title("POST /api/users")
                .payload(Map.of("uri", "/api/users", "method", "POST"))
                .build();

        AuditReplayStepEntity entity = ReplayConverter.toStepEntity(step, "rpl-001");

        assertNotNull(entity);
        assertEquals("rpl-001", entity.getReplayId());
        assertEquals("1", entity.getSequence());
        assertEquals("REQUEST", entity.getStepType());
        assertEquals("POST /api/users", entity.getTitle());
        assertNotNull(entity.getPayloadJson());
        assertTrue(entity.getPayloadJson().contains("uri"));
    }

    @Test
    @DisplayName("toStepEntities — converts all steps to entities")
    void shouldConvertAllStepsToEntities() {
        ReplayStep step1 = ReplayStep.of(1, ReplayStepType.LOGIN, "Login");
        ReplayStep step2 = ReplayStep.of(2, ReplayStepType.AUDIT, "Audit");
        ReplaySession session = ReplaySession.builder()
                .id("rpl-002")
                .timelineId("tl-002")
                .steps(List.of(step1, step2))
                .build();

        List<AuditReplayStepEntity> entities = ReplayConverter.toStepEntities(session);

        assertEquals(2, entities.size());
        assertEquals("rpl-002", entities.get(0).getReplayId());
        assertEquals("1", entities.get(0).getSequence());
        assertEquals("2", entities.get(1).getSequence());
    }

    @Test
    @DisplayName("toDomain — converts entity + step entities to ReplaySession")
    void shouldConvertEntityToDomain() {
        String replayId = UUID.randomUUID().toString();
        AuditReplayEntity entity = new AuditReplayEntity();
        entity.setId(replayId);
        entity.setTimelineId("tl-003");
        entity.setTitle("Domain Test");
        entity.setDuration("3000");

        AuditReplayStepEntity stepEntity = new AuditReplayStepEntity();
        stepEntity.setId(UUID.randomUUID().toString());
        stepEntity.setReplayId(replayId);
        stepEntity.setSequence("1");
        stepEntity.setStepType("REQUEST");
        stepEntity.setTitle("GET /api/test");
        stepEntity.setPayloadJson("{\"uri\":\"/api/test\",\"method\":\"GET\"}");

        ReplaySession session = ReplayConverter.toDomain(entity, List.of(stepEntity));

        assertNotNull(session);
        assertEquals(replayId, session.getId());
        assertEquals("tl-003", session.getTimelineId());
        assertEquals("Domain Test", session.getTitle());
        assertEquals(3000L, session.getDuration());
        assertEquals(1, session.getSteps().size());

        ReplayStep step = session.getSteps().get(0);
        assertEquals(1, step.getSequence());
        assertEquals(ReplayStepType.REQUEST, step.getStepType());
        assertEquals("GET /api/test", step.getTitle());
        assertEquals("/api/test", step.getPayload().get("uri"));
    }

    @Test
    @DisplayName("toDomain — handles null entity gracefully")
    void shouldHandleNullEntity() {
        assertNull(ReplayConverter.toDomain(null, List.of()));
    }

    @Test
    @DisplayName("toDomain — handles empty step list")
    void shouldHandleEmptySteps() {
        AuditReplayEntity entity = new AuditReplayEntity();
        entity.setId("rpl-empty");
        entity.setTimelineId("tl-empty");
        entity.setTitle("Empty");

        ReplaySession session = ReplayConverter.toDomain(entity, List.of());

        assertNotNull(session);
        assertTrue(session.getSteps().isEmpty());
    }

    @Test
    @DisplayName("roundtrip — domain → entity → domain preserves all fields")
    void shouldPreserveFieldsInRoundtrip() {
        ReplayStep step1 = ReplayStep.builder()
                .sequence(1)
                .stepType(ReplayStepType.OPEN_PAGE)
                .title("Open User Management")
                .payload(Map.of("page", "/admin/users"))
                .build();
        ReplayStep step2 = ReplayStep.of(2, ReplayStepType.CLICK_BUTTON, "Click Delete");

        ReplaySession original = ReplaySession.builder()
                .id(UUID.randomUUID().toString())
                .timelineId("tl-roundtrip")
                .title("Roundtrip Test")
                .duration(7000L)
                .steps(List.of(step1, step2))
                .createdAt(LocalDateTime.now())
                .build();

        // Convert to entities
        AuditReplayEntity entity = ReplayConverter.toReplayEntity(original);
        List<AuditReplayStepEntity> stepEntities = ReplayConverter.toStepEntities(original);

        // Convert back
        ReplaySession restored = ReplayConverter.toDomain(entity, stepEntities);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getTimelineId(), restored.getTimelineId());
        assertEquals(original.getTitle(), restored.getTitle());
        assertEquals(original.getDuration(), restored.getDuration());
        assertEquals(2, restored.getSteps().size());
        assertEquals(ReplayStepType.OPEN_PAGE, restored.getSteps().get(0).getStepType());
        assertEquals(ReplayStepType.CLICK_BUTTON, restored.getSteps().get(1).getStepType());
    }

    @Test
    @DisplayName("toStepEntity — null step returns null")
    void shouldHandleNullStep() {
        assertNull(ReplayConverter.toStepEntity(null, "rpl-001"));
    }

    @Test
    @DisplayName("toStepEntity — step with null stepType")
    void shouldHandleNullStepType() {
        ReplayStep step = ReplayStep.builder()
                .sequence(5)
                .title("No type step")
                .build();

        AuditReplayStepEntity entity = ReplayConverter.toStepEntity(step, "rpl-null-type");

        assertNotNull(entity);
        assertEquals("5", entity.getSequence());
        assertEquals("No type step", entity.getTitle());
        assertNull(entity.getStepType());
    }

    @Test
    @DisplayName("toDomain — handles unknown stepType gracefully")
    void shouldHandleUnknownStepType() {
        AuditReplayEntity entity = new AuditReplayEntity();
        entity.setId("rpl-unknown");
        entity.setTimelineId("tl-unknown");

        AuditReplayStepEntity stepEntity = new AuditReplayStepEntity();
        stepEntity.setReplayId("rpl-unknown");
        stepEntity.setSequence("1");
        stepEntity.setStepType("UNKNOWN_TYPE");
        stepEntity.setTitle("Unknown step");

        ReplaySession session = ReplayConverter.toDomain(entity, List.of(stepEntity));

        assertNotNull(session);
        assertEquals(1, session.getSteps().size());
        assertNull(session.getSteps().get(0).getStepType()); // Unknown enum value → null
    }
}