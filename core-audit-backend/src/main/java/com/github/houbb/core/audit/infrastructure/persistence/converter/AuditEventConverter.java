package com.github.houbb.core.audit.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.context.AuditContext;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditEventEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * AuditEventEntity ↔ AuditEvent 双向转换器
 */
public final class AuditEventConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private AuditEventConverter() {
    }

    /**
     * 领域对象 → 持久化实体
     */
    public static AuditEventEntity toEntity(AuditEvent domain) {
        if (domain == null) return null;

        AuditEventEntity entity = new AuditEventEntity();
        entity.setId(domain.getId());
        entity.setModule(domain.getModule() != null ? domain.getModule().name() : null);
        entity.setAction(domain.getAction() != null ? domain.getAction().name() : null);
        entity.setTargetType(domain.getTargetType());
        entity.setTargetId(domain.getTargetId());
        entity.setOperatorId(domain.getOperatorId());
        entity.setOperatorName(domain.getOperatorName());
        entity.setResult(domain.getResult() != null ? domain.getResult().name() : null);
        entity.setDescription(domain.getDescription());
        entity.setClientIp(domain.getClientIp());
        entity.setRequestUri(domain.getRequestUri());
        entity.setRequestMethod(domain.getRequestMethod());
        entity.setTraceId(domain.getTraceId());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt().toString() : null);
        // P1 fields
        entity.setEventType(domain.getEventType() != null ? domain.getEventType().name() : null);
        entity.setEventId(domain.getEventId());
        entity.setSource(domain.getSource());
        entity.setVersion(domain.getVersion());
        entity.setOccurredAt(domain.getOccurredAt() != null ? domain.getOccurredAt().toString() : null);
        entity.setPublished(domain.isPublish() != null ? (domain.isPublish() ? 1 : 0) : 1);
        entity.setPublishTime(domain.getPublishTime() != null ? domain.getPublishTime().toString() : null);
        entity.setPublishResult(domain.getPublishResult());
        entity.setMetadata(serializeMetadata(domain.getMetadata()));
        // P2 context
        entity.setContextJson(serializeContext(domain.getContext()));
        // P9 tenant
        entity.setTenant(domain.getTenant() != null ? domain.getTenant() : "default");
        return entity;
    }

    /**
     * 持久化实体 → 领域对象
     */
    public static AuditEvent toDomain(AuditEventEntity entity) {
        if (entity == null) return null;

        AuditEvent domain = new AuditEvent();
        domain.setId(entity.getId());
        domain.setModule(entity.getModule() != null ? AuditModule.valueOf(entity.getModule()) : null);
        domain.setAction(entity.getAction() != null ? AuditAction.valueOf(entity.getAction()) : null);
        domain.setTargetType(entity.getTargetType());
        domain.setTargetId(entity.getTargetId());
        domain.setOperatorId(entity.getOperatorId());
        domain.setOperatorName(entity.getOperatorName());
        domain.setResult(entity.getResult() != null ? AuditResult.valueOf(entity.getResult()) : null);
        domain.setDescription(entity.getDescription());
        domain.setClientIp(entity.getClientIp());
        domain.setRequestUri(entity.getRequestUri());
        domain.setRequestMethod(entity.getRequestMethod());
        domain.setTraceId(entity.getTraceId());
        domain.setCreatedAt(entity.getCreatedAt() != null ? LocalDateTime.parse(entity.getCreatedAt()) : null);
        // P1 fields
        domain.setEventType(entity.getEventType() != null ? AuditEventType.valueOf(entity.getEventType()) : null);
        domain.setEventId(entity.getEventId());
        domain.setSource(entity.getSource());
        domain.setVersion(entity.getVersion());
        domain.setOccurredAt(entity.getOccurredAt() != null ? LocalDateTime.parse(entity.getOccurredAt()) : null);
        domain.setPublish(entity.getPublished() != null && entity.getPublished() == 1);
        domain.setPublishTime(entity.getPublishTime() != null ? LocalDateTime.parse(entity.getPublishTime()) : null);
        domain.setPublishResult(entity.getPublishResult());
        domain.setMetadata(deserializeMetadata(entity.getMetadata()));
        // P2 context
        domain.setContext(deserializeContext(entity.getContextJson()));
        // P9 tenant
        domain.setTenant(entity.getTenant() != null ? entity.getTenant() : "default");
        return domain;
    }

    private static String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static Map<String, Object> deserializeMetadata(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    private static String serializeContext(AuditContext context) {
        if (context == null) return null;
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static AuditContext deserializeContext(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, AuditContext.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}