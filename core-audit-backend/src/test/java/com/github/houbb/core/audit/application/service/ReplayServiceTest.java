package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.context.AuditContext;
import com.github.houbb.core.audit.application.domain.context.RequestContext;
import com.github.houbb.core.audit.application.domain.enums.*;
import com.github.houbb.core.audit.application.domain.replay.ReplaySession;
import com.github.houbb.core.audit.application.domain.replay.ReplayStep;
import com.github.houbb.core.audit.application.domain.timeline.Timeline;
import com.github.houbb.core.audit.application.port.ReplayRepository;
import com.github.houbb.core.audit.application.port.TimelineRepository;
import com.github.houbb.core.audit.application.replay.ReplayStrategy;
import com.github.houbb.core.audit.infrastructure.replay.DefaultReplayStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplayServiceTest {

    @Mock
    private ReplayRepository replayRepository;

    @Mock
    private TimelineRepository timelineRepository;

    private ReplayService replayService;
    private DefaultReplayStrategy defaultStrategy;

    private AuditEvent buildEvent(String id, AuditAction action, String traceId,
                                   String requestUri, String targetType, String targetId) {
        RequestContext reqCtx = new RequestContext();
        reqCtx.setRequestUri(requestUri);
        reqCtx.setMethod("POST");

        AuditContext context = new AuditContext();
        context.setRequest(reqCtx);

        return AuditEvent.builder()
                .id(id)
                .module(AuditModule.USER)
                .action(action)
                .eventType(AuditEventType.USER_DELETED)
                .operatorId("op-001")
                .operatorName("admin")
                .targetType(targetType)
                .targetId(targetId)
                .result(AuditResult.SUCCESS)
                .traceId(traceId)
                .requestUri(requestUri)
                .requestMethod("POST")
                .description("Delete user " + targetId)
                .createdAt(LocalDateTime.now())
                .context(context)
                .build();
    }

    private Timeline buildTimeline(String id, List<AuditEvent> events) {
        return Timeline.builder()
                .id(id)
                .type(TimelineType.OBJECT)
                .title("Test Timeline")
                .startTime(events.get(0).getCreatedAt())
                .endTime(events.get(events.size() - 1).getCreatedAt())
                .duration(5000L)
                .events(events)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setUp() {
        defaultStrategy = new DefaultReplayStrategy();
        List<ReplayStrategy> strategies = List.of(defaultStrategy);
        replayService = new ReplayService(strategies, replayRepository, timelineRepository);
    }

    @Test
    @DisplayName("build — cache hit returns cached ReplaySession")
    void shouldReturnCachedReplay() {
        String timelineId = "tl-001";
        ReplaySession cached = ReplaySession.builder()
                .id("rpl-001")
                .timelineId(timelineId)
                .title("Cached Replay")
                .duration(3000L)
                .steps(List.of())
                .createdAt(LocalDateTime.now())
                .build();

        when(replayRepository.findByTimelineId(timelineId)).thenReturn(Optional.of(cached));

        Optional<ReplaySession> result = replayService.build(timelineId);

        assertTrue(result.isPresent());
        assertEquals("rpl-001", result.get().getId());
        assertEquals("Cached Replay", result.get().getTitle());
        // Should NOT call timelineRepository or save
        verify(timelineRepository, never()).findById(anyString());
        verify(replayRepository, never()).save(any());
    }

    @Test
    @DisplayName("build — cache miss builds new ReplaySession via strategy")
    void shouldBuildNewReplayOnCacheMiss() {
        String timelineId = "tl-002";
        AuditEvent evt = buildEvent("evt-001", AuditAction.DELETE, "trace-abc",
                "/api/users/delete", "User", "1001");

        Timeline timeline = buildTimeline(timelineId, List.of(evt));

        when(replayRepository.findByTimelineId(timelineId)).thenReturn(Optional.empty());
        when(timelineRepository.findById(timelineId)).thenReturn(Optional.of(timeline));
        when(replayRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<ReplaySession> result = replayService.build(timelineId);

        assertTrue(result.isPresent());
        assertNotNull(result.get().getId());
        assertEquals(timelineId, result.get().getTimelineId());
        assertFalse(result.get().getSteps().isEmpty());
        // Should save the new replay
        verify(replayRepository).save(any());
    }

    @Test
    @DisplayName("build — returns empty when timeline not found")
    void shouldReturnEmptyWhenTimelineNotFound() {
        String timelineId = "tl-missing";
        when(replayRepository.findByTimelineId(timelineId)).thenReturn(Optional.empty());
        when(timelineRepository.findById(timelineId)).thenReturn(Optional.empty());

        Optional<ReplaySession> result = replayService.build(timelineId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("build(Timeline) — generates steps with correct types")
    void shouldGenerateCorrectStepTypes() {
        AuditEvent evt = buildEvent("evt-002", AuditAction.DELETE, "trace-xyz",
                "/api/users/delete", "User", "2001");

        Timeline timeline = buildTimeline("tl-003", List.of(evt));
        when(replayRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReplaySession session = replayService.build(timeline);

        assertNotNull(session);
        List<ReplayStep> steps = session.getSteps();
        assertFalse(steps.isEmpty());

        // Should have REQUEST, DATABASE, AUDIT, EVENT, FINISH steps
        boolean hasRequest = steps.stream().anyMatch(s -> s.getStepType() == ReplayStepType.REQUEST);
        boolean hasDatabase = steps.stream().anyMatch(s -> s.getStepType() == ReplayStepType.DATABASE);
        boolean hasAudit = steps.stream().anyMatch(s -> s.getStepType() == ReplayStepType.AUDIT);
        boolean hasEvent = steps.stream().anyMatch(s -> s.getStepType() == ReplayStepType.EVENT);
        boolean hasFinish = steps.stream().anyMatch(s -> s.getStepType() == ReplayStepType.FINISH);

        assertTrue(hasRequest, "Should have REQUEST step");
        assertTrue(hasDatabase, "Should have DATABASE step");
        assertTrue(hasAudit, "Should have AUDIT step");
        assertTrue(hasEvent, "Should have EVENT step");
        assertTrue(hasFinish, "Should have FINISH step");
    }

    @Test
    @DisplayName("build(Timeline) — LOGIN event generates LOGIN step")
    void shouldGenerateLoginStepForLoginAction() {
        AuditEvent evt = buildEvent("evt-login", AuditAction.LOGIN, "trace-login",
                "/api/auth/login", null, null);
        evt.setEventType(AuditEventType.USER_LOGIN_SUCCESS);
        evt.setDescription("User login success");

        Timeline timeline = buildTimeline("tl-login", List.of(evt));
        when(replayRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReplaySession session = replayService.build(timeline);

        List<ReplayStep> steps = session.getSteps();
        boolean hasLogin = steps.stream().anyMatch(s -> s.getStepType() == ReplayStepType.LOGIN);
        assertTrue(hasLogin, "Should have LOGIN step for LOGIN action");
    }

    @Test
    @DisplayName("getById — returns ReplaySession with steps")
    void shouldReturnReplayById() {
        String replayId = "rpl-010";
        ReplaySession session = ReplaySession.builder()
                .id(replayId)
                .timelineId("tl-010")
                .title("Test")
                .duration(1000L)
                .steps(List.of(ReplayStep.of(1, ReplayStepType.AUDIT, "Test step")))
                .createdAt(LocalDateTime.now())
                .build();

        when(replayRepository.findById(replayId)).thenReturn(Optional.of(session));

        Optional<ReplaySession> result = replayService.getById(replayId);

        assertTrue(result.isPresent());
        assertEquals(replayId, result.get().getId());
        assertEquals(1, result.get().getSteps().size());
    }

    @Test
    @DisplayName("getById — returns empty when not found")
    void shouldReturnEmptyForMissingReplay() {
        when(replayRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<ReplaySession> result = replayService.getById("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getByTimelineId — returns ReplaySession")
    void shouldReturnReplayByTimelineId() {
        String timelineId = "tl-020";
        ReplaySession session = ReplaySession.builder()
                .id("rpl-020")
                .timelineId(timelineId)
                .title("Test")
                .duration(2000L)
                .steps(List.of())
                .createdAt(LocalDateTime.now())
                .build();

        when(replayRepository.findByTimelineId(timelineId)).thenReturn(Optional.of(session));

        Optional<ReplaySession> result = replayService.getByTimelineId(timelineId);

        assertTrue(result.isPresent());
        assertEquals(timelineId, result.get().getTimelineId());
    }

    @Test
    @DisplayName("getSteps — returns steps list")
    void shouldReturnStepsList() {
        String replayId = "rpl-030";
        ReplayStep step1 = ReplayStep.of(1, ReplayStepType.REQUEST, "POST /api/test");
        ReplayStep step2 = ReplayStep.of(2, ReplayStepType.AUDIT, "Audit record");
        ReplaySession session = ReplaySession.builder()
                .id(replayId)
                .timelineId("tl-030")
                .steps(List.of(step1, step2))
                .build();

        when(replayRepository.findById(replayId)).thenReturn(Optional.of(session));

        List<ReplayStep> steps = replayService.getSteps(replayId);

        assertEquals(2, steps.size());
        assertEquals(ReplayStepType.REQUEST, steps.get(0).getStepType());
        assertEquals(ReplayStepType.AUDIT, steps.get(1).getStepType());
    }

    @Test
    @DisplayName("Dashboard — 统计方法正确转发")
    void shouldDelegateDashboardQueries() {
        when(replayRepository.countToday()).thenReturn(15L);
        when(replayRepository.avgStepsToday()).thenReturn(8.5);
        when(replayRepository.maxStepsToday()).thenReturn(42);
        when(replayRepository.avgDurationToday()).thenReturn(3200.0);

        assertEquals(15L, replayService.countToday());
        assertEquals(8.5, replayService.avgStepsToday(), 0.01);
        assertEquals(42, replayService.maxStepsToday());
        assertEquals(3200.0, replayService.avgDurationToday(), 0.01);
    }

    @Test
    @DisplayName("fault isolation — strategy failure does not crash service")
    void shouldHandleStrategyFailure() {
        AuditEvent evt = buildEvent("evt-err", AuditAction.UPDATE, "trace-err",
                "/api/test", "Config", "5001");
        Timeline timeline = buildTimeline("tl-err", List.of(evt));

        // Create a service with a throwing strategy
        ReplayStrategy throwingStrategy = new ReplayStrategy() {
            @Override
            public boolean supports(Timeline t) { return true; }
            @Override
            public ReplaySession build(Timeline t) {
                throw new RuntimeException("Simulated strategy failure");
            }
        };

        ReplayService serviceWithThrowing = new ReplayService(
                List.of(throwingStrategy, defaultStrategy),
                replayRepository, timelineRepository);

        when(replayRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReplaySession session = serviceWithThrowing.build(timeline);

        // Should fall through to default strategy
        assertNotNull(session);
        assertFalse(session.getSteps().isEmpty());
    }
}