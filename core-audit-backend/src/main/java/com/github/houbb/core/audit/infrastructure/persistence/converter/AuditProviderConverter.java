package com.github.houbb.core.audit.infrastructure.persistence.converter;

import com.github.houbb.core.audit.application.domain.enterprise.AuditProvider;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditProviderEntity;

/**
 * P9 AuditProvider 领域对象与实体转换器
 */
public class AuditProviderConverter {

    private AuditProviderConverter() {}

    public static AuditProvider toDomain(AuditProviderEntity entity) {
        if (entity == null) return null;
        return AuditProvider.builder()
                .id(entity.getId())
                .plugin(entity.getPlugin())
                .providerClass(entity.getProviderClass())
                .providerType(entity.getProviderType())
                .version(entity.getVersion())
                .tenant(entity.getTenant())
                .description(entity.getDescription())
                .author(entity.getAuthor())
                .configJson(entity.getConfigJson())
                .status(entity.getStatus())
                .installedAt(entity.getInstalledAt())
                .build();
    }

    public static AuditProviderEntity toEntity(AuditProvider domain) {
        if (domain == null) return null;
        AuditProviderEntity entity = new AuditProviderEntity();
        entity.setId(domain.getId());
        entity.setPlugin(domain.getPlugin());
        entity.setProviderClass(domain.getProviderClass());
        entity.setProviderType(domain.getProviderType());
        entity.setVersion(domain.getVersion());
        entity.setTenant(domain.getTenant());
        entity.setDescription(domain.getDescription());
        entity.setAuthor(domain.getAuthor());
        entity.setConfigJson(domain.getConfigJson());
        entity.setStatus(domain.getStatus());
        entity.setInstalledAt(domain.getInstalledAt());
        return entity;
    }
}