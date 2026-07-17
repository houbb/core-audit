package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.context.AuditContext;
import com.github.houbb.core.audit.application.domain.context.BusinessContext;
import com.github.houbb.core.audit.application.domain.context.RequestContext;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.application.domain.enums.TimelineType;
import com.github.houbb.core.audit.application.domain.timeline.Timeline;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;
import com.github.houbb.core.audit.application.port.TimelineRepository;
import com.github.houbb.core.audit.application.timeline.TimelineStrategy;
import com.github.houbb.core.audit.infrastructure.timeline.OperatorTimelineStrategy;
import com.github.houbb.core.audit.infrastructure.timeline.ResourceTimelineStrategy;
import com.github.houbb.core.audit.infrastructure.timeline.TraceTimelineStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimelineServiceTest {

    @Mock
    private TimelineRepository timelineRepository;

    private TimelineService timelineService;

    private TraceTimelineStrategy traceStrategy;
    private ResourceTimelineStrategy resourceStrategy;
    private OperatorTimelineStrategy operatorStrategy;

    private AuditEvent buildEvent(String id, String traceId, String operatorId,
                                   String targetType, String targetId) {
        return AuditEvent.builder()
                .id(id)
                .module(AuditModule.USER)
                .action(AuditAction.UPDATE)
                .eventType(AuditEventType.USER_UPDATED)
                .operatorId(operatorId)
                .operatorName("test-operator")
                .targetType(targetType)
                .targetId(targetId)
                .result(AuditResult.SUCCESS)
                .traceId(traceId)
                .description("test event")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setUp() {
        traceStrategy = new TraceTimelineStrategy();
        resourceStrategy = new ResourceTimelineStrategy();
        operatorStrategy = new OperatorTimelineStrategy();
        List<TimelineStrategy> strategies = List.of(traceStrategy, resourceStrategy, operatorStrategy);
        timelineService = new TimelineService(strategies, timelineRepository);
    }

    @Test
    @DisplayName("append — 多归属：一个 event 同时追加到 Trace + Resource + Operator Timeline")
    void shouldAppendToMultipleTimelines() {
        AuditEvent event = buildEvent("evt-001", "trace-abc", "op-001", "User", "1001");

        // 模拟 findOrCreate: 每种 Timeline 都不存在，需新建
        when(timelineRepository.findByKey(any())).thenReturn(List.of());
        when(timelineRepository.getMaxSequence(anyString())).thenReturn(0);
        doNothing().when(timelineRepository).appendEvent(anyString(), anyString(), anyInt());
        when(timelineRepository.findById(anyString())).thenReturn(Optional.empty());
        doAnswer(inv -> {
            Timeline t = inv.getArgument(0);
            return t;
        }).when(timelineRepository).save(any());

        timelineService.append(event);

        // 验证 3 个 strategy 都被调用 → 3 次 save（新建）+ 3 次 appendEvent
        verify(timelineRepository, atLeast(3)).save(any());
        verify(timelineRepository, atLeast(3)).appendEvent(anyString(), eq("evt-001"), anyInt());
    }

    @Test
    @DisplayName("append — 无 traceId 时只匹配 Resource + Operator")
    void shouldSkipTraceWhenNoTraceId() {
        AuditEvent event = buildEvent("evt-002", null, "op-002", "File", "2001");

        when(timelineRepository.findByKey(any())).thenReturn(List.of());
        when(timelineRepository.getMaxSequence(anyString())).thenReturn(0);
        doNothing().when(timelineRepository).appendEvent(anyString(), anyString(), anyInt());
        when(timelineRepository.findById(anyString())).thenReturn(Optional.empty());
        doAnswer(inv -> inv.getArgument(0)).when(timelineRepository).save(any());

        timelineService.append(event);

        // 只匹配 Resource + Operator（2 个），Trace 被跳过
        verify(timelineRepository, atLeast(2)).save(any());
        verify(timelineRepository, atLeast(2)).appendEvent(anyString(), eq("evt-002"), anyInt());
    }

    @Test
    @DisplayName("getById — 返回完整 Timeline（含 events）")
    void shouldReturnTimelineWithEvents() {
        String timelineId = "tl-001";
        AuditEvent evt = buildEvent("evt-003", "trace-xyz", "op-003", "Role", "3001");

        Timeline timeline = Timeline.builder()
                .id(timelineId)
                .type(TimelineType.REQUEST)
                .title("REQUEST — trace-xyz")
                .startTime(LocalDateTime.now().minusMinutes(5))
                .endTime(LocalDateTime.now())
                .duration(300000L)
                .events(List.of(evt))
                .createdAt(LocalDateTime.now())
                .build();

        when(timelineRepository.findById(timelineId)).thenReturn(Optional.of(timeline));

        Optional<Timeline> result = timelineService.getById(timelineId);

        assertTrue(result.isPresent());
        assertEquals(timelineId, result.get().getId());
        assertEquals(TimelineType.REQUEST, result.get().getType());
        assertEquals(1, result.get().getEvents().size());
    }

    @Test
    @DisplayName("getById — 不存在返回 empty")
    void shouldReturnEmptyForMissingTimeline() {
        when(timelineRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Timeline> result = timelineService.getById("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("buildStory — 自动生成 title")
    void shouldAutoGenerateTitle() {
        String timelineId = "tl-002";
        AuditEvent evt1 = buildEvent("evt-004", "trace-abc", "op-004", "Config", "4001");
        evt1.setCreatedAt(LocalDateTime.of(2026, 7, 17, 10, 0));
        AuditEvent evt2 = buildEvent("evt-005", "trace-abc", "op-004", "Config", "4002");
        evt2.setCreatedAt(LocalDateTime.of(2026, 7, 17, 10, 5));

        Timeline timeline = Timeline.builder()
                .id(timelineId)
                .type(TimelineType.OBJECT)
                .startTime(evt1.getCreatedAt())
                .endTime(evt2.getCreatedAt())
                .events(new ArrayList<>(List.of(evt2, evt1))) // intentionally out of order
                .build();

        when(timelineRepository.findById(timelineId)).thenReturn(Optional.of(timeline));

        Optional<Timeline> result = timelineService.buildStory(timelineId);

        assertTrue(result.isPresent());
        assertNotNull(result.get().getTitle());
        assertTrue(result.get().getTitle().contains("2 Events"));
    }

    @Test
    @DisplayName("Dashboard — 统计方法正确转发")
    void shouldDelegateDashboardQueries() {
        when(timelineRepository.countToday()).thenReturn(42L);
        when(timelineRepository.avgLengthToday()).thenReturn(12.5);
        when(timelineRepository.maxLengthToday()).thenReturn(86);
        when(timelineRepository.avgDurationToday()).thenReturn(2800.0);

        assertEquals(42L, timelineService.countToday());
        assertEquals(12.5, timelineService.avgLengthToday(), 0.01);
        assertEquals(86, timelineService.maxLengthToday());
        assertEquals(2800.0, timelineService.avgDurationToday(), 0.01);
    }
}