package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.enterprise.AuditProvider;

import java.util.List;
import java.util.Optional;

/**
 * P9 Audit Provider（Marketplace） 仓储端口
 * <p>管理 SPI 插件的安装、卸载、查询。</p>
 */
public interface AuditProviderRepository {

    AuditProvider save(AuditProvider provider);

    Optional<AuditProvider> findById(String id);

    Optional<AuditProvider> findByPluginAndTenant(String plugin, String tenant);

    List<AuditProvider> findAllByTenant(String tenant);

    List<AuditProvider> findAllByTypeAndTenant(String providerType, String tenant);

    List<AuditProvider> findAllActiveByTenant(String tenant);

    long countByTenant(String tenant);

    void updateStatus(String id, String status);

    void deleteById(String id);
}