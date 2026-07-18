package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.enterprise.AuditSource;

import java.util.List;
import java.util.Optional;

/**
 * P9 Audit Source 仓储端口
 * <p>管理审计来源系统的注册与查询。</p>
 */
public interface AuditSourceRepository {

    AuditSource save(AuditSource source);

    Optional<AuditSource> findById(String id);

    Optional<AuditSource> findByNameAndTenant(String name, String tenant);

    List<AuditSource> findAllByTenant(String tenant);

    List<AuditSource> findAllActiveByTenant(String tenant);

    long countByTenant(String tenant);

    void updateLastSeenAt(String id, String lastSeenAt);

    void deleteById(String id);
}