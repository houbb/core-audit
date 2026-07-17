package com.github.houbb.core.audit.infrastructure.replay;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.ReplayStepType;
import com.github.houbb.core.audit.application.domain.replay.ReplaySession;
import com.github.houbb.core.audit.application.domain.replay.ReplayStep;
import com.github.houbb.core.audit.application.domain.timeline.Timeline;
import com.github.houbb.core.audit.application.replay.ReplayStrategy;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 默认 Replay 构建策略
 * <p>通用策略，适用于所有 Timeline 类型。从 AuditEvent 序列生成 ReplayStep 序列。</p>
 *
 * <p>构建规则：</p>
 * <ul>
 *   <li>LOGIN action → LOGIN step</li>
 *   <li>requestUri 存在 → REQUEST step</li>
 *   <li>CREATE / UPDATE / DELETE action → DATABASE step</li>
 *   <li>每个 event → AUDIT step</li>
 *   <li>eventType 存在 → EVENT step</li>
 *   <li>最后 → FINISH step</li>
 * </ul>
 */
@Component
public class DefaultReplayStrategy implements ReplayStrategy {

    @Override
    public boolean supports(Timeline timeline) {
        // 通用策略，始终可用
        return true;
    }

    @Override
    public ReplaySession build(Timeline timeline) {
        List<AuditEvent> events = timeline.getEvents();
        if (events == null || events.isEmpty()) {
            return ReplaySession.builder()
                    .id(UUID.randomUUID().toString())
                    .timelineId(timeline.getId())
                    .title(timeline.getTitle() != null ? timeline.getTitle() : "Empty Replay")
                    .duration(0L)
                    .steps(Collections.emptyList())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
        }

        // 按 createdAt 排序
        List<AuditEvent> sorted = new ArrayList<>(events);
        sorted.sort(Comparator.comparing(
                e -> e.getCreatedAt() != null ? e.getCreatedAt() : java.time.LocalDateTime.MIN));

        List<ReplayStep> steps = new ArrayList<>();
        int seq = 1;

        for (AuditEvent event : sorted) {
            AuditAction action = event.getAction();
            String desc = event.getDescription() != null ? event.getDescription() : "";

            // Step: LOGIN
            if (action == AuditAction.LOGIN || action == AuditAction.LOGOUT) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("operatorId", event.getOperatorId());
                payload.put("operatorName", event.getOperatorName());
                payload.put("result", event.getResult() != null ? event.getResult().name() : null);
                steps.add(ReplayStep.of(seq++, ReplayStepType.LOGIN,
                        (action == AuditAction.LOGIN ? "登录" : "登出") + " — " + event.getOperatorName(),
                        payload));
            }

            // Step: OPEN_PAGE — inferred from request URI
            if (event.getRequestUri() != null && !event.getRequestUri().isBlank()) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("uri", event.getRequestUri());
                payload.put("method", event.getRequestMethod());
                steps.add(ReplayStep.of(seq++, ReplayStepType.OPEN_PAGE,
                        "打开页面: " + event.getRequestUri(), payload));
            }

            // Step: REQUEST — always present when URI exists
            if (event.getRequestUri() != null && !event.getRequestUri().isBlank()) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("uri", event.getRequestUri());
                payload.put("method", event.getRequestMethod());
                payload.put("ip", event.getClientIp());
                steps.add(ReplayStep.of(seq++, ReplayStepType.REQUEST,
                        event.getRequestMethod() + " " + event.getRequestUri(), payload));
            }

            // Step: INPUT — for CREATE/UPDATE actions
            if (action == AuditAction.CREATE || action == AuditAction.UPDATE) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("targetType", event.getTargetType());
                payload.put("targetId", event.getTargetId());
                steps.add(ReplayStep.of(seq++, ReplayStepType.INPUT,
                        action == AuditAction.CREATE ? "创建 " + event.getTargetType() : "修改 " + event.getTargetType(),
                        payload));
            }

            // Step: DATABASE — for data-modifying actions
            if (action == AuditAction.CREATE || action == AuditAction.UPDATE || action == AuditAction.DELETE) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("action", action.name());
                payload.put("targetType", event.getTargetType());
                payload.put("targetId", event.getTargetId());
                payload.put("result", event.getResult() != null ? event.getResult().name() : null);
                steps.add(ReplayStep.of(seq++, ReplayStepType.DATABASE,
                        getDatabaseActionLabel(action) + " " + event.getTargetType() + "#" + event.getTargetId(),
                        payload));
            }

            // Step: AUDIT — always
            {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("auditId", event.getId());
                payload.put("module", event.getModule() != null ? event.getModule().name() : null);
                payload.put("action", event.getAction() != null ? event.getAction().name() : null);
                payload.put("result", event.getResult() != null ? event.getResult().name() : null);
                payload.put("description", desc);
                steps.add(ReplayStep.of(seq++, ReplayStepType.AUDIT,
                        "审计: " + truncate(desc, 80), payload));
            }

            // Step: EVENT — if eventType present
            if (event.getEventType() != null) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("eventType", event.getEventType().name());
                payload.put("source", event.getSource());
                payload.put("eventId", event.getEventId());
                steps.add(ReplayStep.of(seq++, ReplayStepType.EVENT,
                        "事件: " + event.getEventType().name(), payload));
            }
        }

        // Step: FINISH — always last
        steps.add(ReplayStep.of(seq, ReplayStepType.FINISH, "完成",
                Map.of("totalSteps", steps.size(), "totalEvents", sorted.size())));

        return ReplaySession.builder()
                .id(UUID.randomUUID().toString())
                .timelineId(timeline.getId())
                .title(timeline.getTitle() != null ? timeline.getTitle() : "Replay: " + sorted.size() + " Steps")
                .duration(timeline.getDuration())
                .steps(steps)
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    private String getDatabaseActionLabel(AuditAction action) {
        switch (action) {
            case CREATE: return "INSERT";
            case UPDATE: return "UPDATE";
            case DELETE: return "DELETE";
            default: return action.name();
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}