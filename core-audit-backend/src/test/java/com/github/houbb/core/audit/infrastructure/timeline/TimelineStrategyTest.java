package com.github.houbb.core.audit.infrastructure.timeline;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.context.AuditContext;
import com.github.houbb.core.audit.application.domain.context.BusinessContext;
import com.github.houbb.core.audit.application.domain.context.RequestContext;
import com.github.houbb.core.audit.application.domain.enums.*;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimelineStrategyTest {

    private AuditEvent buildEvent(String traceId, String operatorId,
                                   String targetType, String targetId,
                                   String sessionId, String workflowId) {
        AuditEvent event = AuditEvent.builder()
                .id("evt-test")
                .module(AuditModule.USER)
                .action(AuditAction.UPDATE)
                .eventType(AuditEventType.USER_UPDATED)
                .operatorId(operatorId)
                .targetType(targetType)
                .targetId(targetId)
                .result(AuditResult.SUCCESS)
                .traceId(traceId)
                .description("test")
                .build();

        AuditContext context = new AuditContext();
        RequestContext reqCtx = new RequestContext();
        reqCtx.setSessionId(sessionId);
        context.setRequest(reqCtx);

        BusinessContext bizCtx = new BusinessContext();
        bizCtx.setWorkflowId(workflowId);
        context.setBusiness(bizCtx);

        event.setContext(context);
        return event;
    }

    @Test
    @DisplayName("TraceTimelineStrategy — 有 traceId 时匹配")
    void traceStrategyShouldMatchWhenTraceIdPresent() {
        TraceTimelineStrategy strategy = new TraceTimelineStrategy();
        AuditEvent event = buildEvent("trace-123", null, null, null, null, null);

        assertTrue(strategy.supports(event));
        assertEquals(TimelineKey.of("REQUEST", "trace-123"), strategy.resolve(event));
        assertEquals(100, strategy.order());
    }

    @Test
    @DisplayName("TraceTimelineStrategy — 无 traceId 时不匹配")
    void traceStrategyShouldNotMatchWhenTraceIdNull() {
        TraceTimelineStrategy strategy = new TraceTimelineStrategy();
        AuditEvent event = buildEvent(null, null, null, null, null, null);

        assertFalse(strategy.supports(event));
    }

    @Test
    @DisplayName("ResourceTimelineStrategy — 有 targetType+targetId 时匹配")
    void resourceStrategyShouldMatch() {
        ResourceTimelineStrategy strategy = new ResourceTimelineStrategy();
        AuditEvent event = buildEvent(null, null, "User", "1001", null, null);

        assertTrue(strategy.supports(event));
        assertEquals(TimelineKey.of("OBJECT", "User#1001"), strategy.resolve(event));
        assertEquals(300, strategy.order());
    }

    @Test
    @DisplayName("ResourceTimelineStrategy — 无 target 时不匹配")
    void resourceStrategyShouldNotMatchWhenNoTarget() {
        ResourceTimelineStrategy strategy = new ResourceTimelineStrategy();
        AuditEvent event = buildEvent(null, null, null, null, null, null);

        assertFalse(strategy.supports(event));
    }

    @Test
    @DisplayName("OperatorTimelineStrategy — 有 operatorId 时匹配")
    void operatorStrategyShouldMatch() {
        OperatorTimelineStrategy strategy = new OperatorTimelineStrategy();
        AuditEvent event = buildEvent(null, "op-001", null, null, null, null);

        assertTrue(strategy.supports(event));
        assertEquals(TimelineKey.of("USER", "op-001"), strategy.resolve(event));
        assertEquals(400, strategy.order());
    }

    @Test
    @DisplayName("OperatorTimelineStrategy — 无 operatorId 时不匹配")
    void operatorStrategyShouldNotMatch() {
        OperatorTimelineStrategy strategy = new OperatorTimelineStrategy();
        AuditEvent event = buildEvent(null, null, null, null, null, null);

        assertFalse(strategy.supports(event));
    }

    @Test
    @DisplayName("SessionTimelineStrategy — 有 sessionId 时匹配")
    void sessionStrategyShouldMatch() {
        SessionTimelineStrategy strategy = new SessionTimelineStrategy();
        AuditEvent event = buildEvent(null, null, null, null, "sess-abc", null);

        assertTrue(strategy.supports(event));
        assertEquals(TimelineKey.of("SESSION", "sess-abc"), strategy.resolve(event));
    }

    @Test
    @DisplayName("SessionTimelineStrategy — 无 sessionId 时不匹配")
    void sessionStrategyShouldNotMatch() {
        SessionTimelineStrategy strategy = new SessionTimelineStrategy();
        AuditEvent event = buildEvent(null, null, null, null, null, null);

        assertFalse(strategy.supports(event));
    }

    @Test
    @DisplayName("WorkflowTimelineStrategy — BusinessContext 中有 workflowId 时匹配")
    void workflowStrategyShouldMatchFromBusinessContext() {
        WorkflowTimelineStrategy strategy = new WorkflowTimelineStrategy();
        AuditEvent event = buildEvent(null, null, null, null, null, "wf-001");

        assertTrue(strategy.supports(event));
        assertEquals(TimelineKey.of("WORKFLOW", "wf-001"), strategy.resolve(event));
    }

    @Test
    @DisplayName("WorkflowTimelineStrategy — 无 workflowId 时不匹配")
    void workflowStrategyShouldNotMatch() {
        WorkflowTimelineStrategy strategy = new WorkflowTimelineStrategy();
        AuditEvent event = buildEvent(null, null, null, null, null, null);

        assertFalse(strategy.supports(event));
    }
}
