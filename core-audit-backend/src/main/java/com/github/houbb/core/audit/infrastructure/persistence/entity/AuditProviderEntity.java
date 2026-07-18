package com.github.houbb.core.audit.infrastructure.persistence.entity;

/**
 * P9 插件 Provider 持久化实体
 * <p>映射 audit_provider 表。</p>
 */
public class AuditProviderEntity {

    private String id;
    private String plugin;
    private String providerClass;
    private String providerType;
    private String version;
    private String tenant;
    private String description;
    private String author;
    private String configJson;
    private String status;
    private String installedAt;
    private String createTime;
    private String updateTime;
    private String createUser;
    private String updateUser;

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
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public String getUpdateUser() { return updateUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
}