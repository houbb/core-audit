package com.github.houbb.core.audit.infrastructure.persistence.converter;

import com.github.houbb.core.audit.application.domain.enterprise.AuditSource;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSourceEntity;

/**
 * P9 AuditSource 领域对象与实体转换器
 */
public class AuditSourceConverter {

    private AuditSourceConverter() {}

    public static AuditSource toDomain(AuditSourceEntity entity) {
        if (entity == null) return null;
        return AuditSource.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .version(entity.getVersion())
                .tenant(entity.getTenant())
                .description(entity.getDescription())
                .endpoint(entity.getEndpoint())
                .authToken(entity.getAuthToken())
                .status(entity.getStatus())
                .registeredAt(entity.getRegisteredAt())
                .lastSeenAt(entity.getLastSeenAt())
                .metadataJson(entity.getMetadataJson())
                .build();
    }

    public static AuditSourceEntity toEntity(AuditSource domain) {
        if (domain == null) return null;
        AuditSourceEntity entity = new AuditSourceEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setVersion(domain.getVersion());
        entity.setTenant(domain.getTenant());
        entity.setDescription(domain.getDescription());
        entity.setEndpoint(domain.getEndpoint());
        entity.setAuthToken(domain.getAuthToken());
        entity.setStatus(domain.getStatus());
        entity.setRegisteredAt(domain.getRegisteredAt());
        entity.setLastSeenAt(domain.getLastSeenAt());
        entity.setMetadataJson(domain.getMetadataJson());
        return entity;
    }
}