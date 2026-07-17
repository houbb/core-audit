package com.github.houbb.core.audit.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.enums.ReplayStepType;
import com.github.houbb.core.audit.application.domain.replay.ReplaySession;
import com.github.houbb.core.audit.application.domain.replay.ReplayStep;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditReplayEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditReplayStepEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Replay 转换器
 * <p>静态工具类，负责 ReplaySession / ReplayStep 领域对象与 Entity 之间的双向转换。</p>
 * <p>遵循 AuditEventConverter / TimelineConverter / DiffConverter 的模式。</p>
 */
public final class ReplayConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private ReplayConverter() {
        // 工具类，禁止实例化
    }

    // ======== Domain → Entity ========

    /**
     * ReplaySession → AuditReplayEntity
     */
    public static AuditReplayEntity toReplayEntity(ReplaySession domain) {
        if (domain == null) return null;
        AuditReplayEntity entity = new AuditReplayEntity();
        entity.setId(domain.getId());
        entity.setTimelineId(domain.getTimelineId());
        entity.setTitle(domain.getTitle());
        if (domain.getDuration() != null) {
            entity.setDuration(String.valueOf(domain.getDuration()));
        }
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt().format(FORMATTER));
        }
        return entity;
    }

    /**
     * ReplayStep → AuditReplayStepEntity
     */
    public static AuditReplayStepEntity toStepEntity(ReplayStep step, String replayId) {
        if (step == null) return null;
        AuditReplayStepEntity entity = new AuditReplayStepEntity();
        entity.setReplayId(replayId);
        entity.setSequence(String.valueOf(step.getSequence()));
        entity.setStepType(step.getStepType() != null ? step.getStepType().name() : null);
        entity.setTitle(step.getTitle());
        entity.setPayloadJson(serializePayload(step.getPayload()));
        return entity;
    }

    /**
     * ReplaySession → 步骤实体列表
     */
    public static List<AuditReplayStepEntity> toStepEntities(ReplaySession session) {
        if (session == null || session.getSteps() == null) return Collections.emptyList();
        return session.getSteps().stream()
                .map(step -> toStepEntity(step, session.getId()))
                .collect(Collectors.toList());
    }

    // ======== Entity → Domain ========

    /**
     * AuditReplayEntity + List&lt;AuditReplayStepEntity&gt; → ReplaySession
     */
    public static ReplaySession toDomain(AuditReplayEntity entity, List<AuditReplayStepEntity> stepEntities) {
        if (entity == null) return null;
        ReplaySession session = new ReplaySession();
        session.setId(entity.getId());
        session.setTimelineId(entity.getTimelineId());
        session.setTitle(entity.getTitle());
        if (entity.getDuration() != null && !entity.getDuration().isBlank()) {
            try {
                session.setDuration(Long.parseLong(entity.getDuration()));
            } catch (NumberFormatException e) {
                // 解析失败忽略
            }
        }
        if (entity.getCreatedAt() != null && !entity.getCreatedAt().isBlank()) {
            try {
                session.setCreatedAt(LocalDateTime.parse(entity.getCreatedAt(), FORMATTER));
            } catch (Exception e) {
                // 解析失败忽略
            }
        }

        if (stepEntities != null && !stepEntities.isEmpty()) {
            List<ReplayStep> steps = stepEntities.stream()
                    .map(ReplayConverter::toStepDomain)
                    .sorted((a, b) -> Integer.compare(a.getSequence(), b.getSequence()))
                    .collect(Collectors.toList());
            session.setSteps(steps);
        }

        return session;
    }

    /**
     * AuditReplayStepEntity → ReplayStep
     */
    private static ReplayStep toStepDomain(AuditReplayStepEntity entity) {
        if (entity == null) return null;
        int seq = 0;
        try {
            seq = Integer.parseInt(entity.getSequence());
        } catch (NumberFormatException e) {
            // fallback to 0
        }

        ReplayStepType stepType = null;
        if (entity.getStepType() != null && !entity.getStepType().isBlank()) {
            try {
                stepType = ReplayStepType.valueOf(entity.getStepType());
            } catch (IllegalArgumentException e) {
                // 未知类型忽略
            }
        }

        Map<String, Object> payload = deserializePayload(entity.getPayloadJson());

        return ReplayStep.builder()
                .sequence(seq)
                .stepType(stepType)
                .title(entity.getTitle())
                .payload(payload)
                .build();
    }

    // ======== JSON Helpers ========

    private static String serializePayload(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static Map<String, Object> deserializePayload(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }
}