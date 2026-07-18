package com.github.houbb.core.audit.application.domain.enterprise;

/**
 * P9 审计插件/Marketplace Provider 领域对象
 * <p>表示已安装的 SPI 插件。</p>
 */
public class AuditProvider {

    private String id;
    private String plugin;
    private String providerClass;
    private String providerType;   // PROVIDER / TIMELINE_STRATEGY / REPLAY_STRATEGY / COMPLIANCE_PROVIDER / AUDIT_AGENT
    private String version;
    private String tenant;
    private String description;
    private String author;
    private String configJson;
    private String status;         // ACTIVE / INACTIVE / ERROR
    private String installedAt;

    private AuditProvider() {}

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final AuditProvider provider = new AuditProvider();
        public Builder id(String v) { provider.id = v; return this; }
        public Builder plugin(String v) { provider.plugin = v; return this; }
        public Builder providerClass(String v) { provider.providerClass = v; return this; }
        public Builder providerType(String v) { provider.providerType = v; return this; }
        public Builder version(String v) { provider.version = v; return this; }
        public Builder tenant(String v) { provider.tenant = v; return this; }
        public Builder description(String v) { provider.description = v; return this; }
        public Builder author(String v) { provider.author = v; return this; }
        public Builder configJson(String v) { provider.configJson = v; return this; }
        public Builder status(String v) { provider.status = v; return this; }
        public Builder installedAt(String v) { provider.installedAt = v; return this; }
        public AuditProvider build() { return provider; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPlugin() { return plugin; }
    public void setPlugin(String plugin) { this.plugin = plugin; }
    public String getProviderClass() { return providerClass; }
    public void setProviderClass(String providerClass) { this.providerClass = providerClass; }
    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getTenant() { return tenant; }
    public void setTenant(String tenant) { this.tenant = tenant; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInstalledAt() { return installedAt; }
    public void setInstalledAt(String installedAt) { this.installedAt = installedAt; }
}
