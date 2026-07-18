package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enterprise.AuditSource;
import com.github.houbb.core.audit.application.port.AuditSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * P9 Source Management Service
 * <p>管理审计来源系统的注册、心跳更新、查询。</p>
 */
@Service
public class SourceService {

    private static final Logger log = LoggerFactory.getLogger(SourceService.class);

    private final AuditSourceRepository sourceRepository;

    public SourceService(AuditSourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    /**
     * 注册或更新来源系统
     */
    public AuditSource register(String name, String type, String version, String tenant,
                                 String description, String endpoint) {
        // Check existing
        Optional<AuditSource> existing = sourceRepository.findByNameAndTenant(name, tenant);
        AuditSource source;
        if (existing.isPresent()) {
            source = existing.get();
            source.setVersion(version);
            source.setEndpoint(endpoint);
            source.setDescription(description);
            source.setLastSeenAt(now());
        } else {
            source = AuditSource.builder()
                    .name(name)
                    .type(type)
                    .version(version)
                    .tenant(tenant)
                    .description(description)
                    .endpoint(endpoint)
                    .status("ACTIVE")
                    .registeredAt(now())
                    .lastSeenAt(now())
                    .build();
        }
        AuditSource saved = sourceRepository.save(source);
        log.info("Source registered/updated: name={}, tenant={}, status={}", name, tenant, saved.getStatus());
        return saved;
    }

    /**
     * 心跳更新
     */
    public void heartbeat(String sourceId) {
        sourceRepository.updateLastSeenAt(sourceId, now());
    }

    /**
     * 获取租户下所有活跃来源
     */
    public List<AuditSource> getActiveSources(String tenant) {
        return sourceRepository.findAllActiveByTenant(tenant);
    }

    /**
     * 获取租户下所有来源
     */
    public List<AuditSource> getAllSources(String tenant) {
        return sourceRepository.findAllByTenant(tenant);
    }

    /**
     * 来源计数
     */
    public long countByTenant(String tenant) {
        return sourceRepository.countByTenant(tenant);
    }

    /**
     * 删除来源
     */
    public void deleteSource(String id) {
        sourceRepository.deleteById(id);
        log.info("Source deleted: id={}", id);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }
}