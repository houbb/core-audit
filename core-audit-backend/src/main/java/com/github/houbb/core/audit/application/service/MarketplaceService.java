package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.enterprise.AuditProvider;
import com.github.houbb.core.audit.application.port.AuditProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * P9 Marketplace Service
 * <p>管理 SPI 插件/Marketplace 的安装、卸载、启用、禁用。</p>
 */
@Service
public class MarketplaceService {

    private static final Logger log = LoggerFactory.getLogger(MarketplaceService.class);

    private final AuditProviderRepository providerRepository;

    public MarketplaceService(AuditProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    /**
     * 安装插件
     */
    public AuditProvider install(String plugin, String providerClass, String providerType,
                                  String version, String tenant, String description,
                                  String author, String configJson) {
        Optional<AuditProvider> existing = providerRepository.findByPluginAndTenant(plugin, tenant);
        if (existing.isPresent()) {
            log.warn("Plugin '{}' already installed for tenant '{}', updating", plugin, tenant);
            AuditProvider p = existing.get();
            p.setProviderClass(providerClass);
            p.setVersion(version);
            p.setDescription(description);
            p.setConfigJson(configJson);
            return providerRepository.save(p);
        }

        AuditProvider provider = AuditProvider.builder()
                .plugin(plugin)
                .providerClass(providerClass)
                .providerType(providerType)
                .version(version)
                .tenant(tenant)
                .description(description)
                .author(author)
                .configJson(configJson)
                .status("ACTIVE")
                .installedAt(now())
                .build();
        AuditProvider saved = providerRepository.save(provider);
        log.info("Plugin installed: plugin={}, tenant={}, type={}", plugin, tenant, providerType);
        return saved;
    }

    /**
     * 卸载插件
     */
    public void uninstall(String id) {
        providerRepository.deleteById(id);
        log.info("Plugin uninstalled: id={}", id);
    }

    /**
     * 更新插件状态
     */
    public void updateStatus(String id, String status) {
        providerRepository.updateStatus(id, status);
        log.info("Plugin status updated: id={}, status={}", id, status);
    }

    /**
     * 获取租户下所有已安装插件
     */
    public List<AuditProvider> getInstalledProviders(String tenant) {
        return providerRepository.findAllByTenant(tenant);
    }

    /**
     * 获取租户下活跃插件
     */
    public List<AuditProvider> getActiveProviders(String tenant) {
        return providerRepository.findAllActiveByTenant(tenant);
    }

    /**
     * 按类型获取插件
     */
    public List<AuditProvider> getProvidersByType(String providerType, String tenant) {
        return providerRepository.findAllByTypeAndTenant(providerType, tenant);
    }

    /**
     * 插件计数
     */
    public long countByTenant(String tenant) {
        return providerRepository.countByTenant(tenant);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }
}