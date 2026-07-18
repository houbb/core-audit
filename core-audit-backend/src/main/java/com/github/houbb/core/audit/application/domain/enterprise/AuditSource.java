package com.github.houbb.core.audit.application.domain.enterprise;

/**
 * P9 审计来源系统领域对象
 * <p>表示接入 core-audit 的外部系统身份信息。</p>
 */
public class AuditSource {

    private String id;
    private String name;
    private String type;        // INTERNAL / EXTERNAL / PLUGIN / AGENT / THIRD_PARTY
    private String version;
    private String tenant;
    private String description;
    private String endpoint;
    private String authToken;
    private String status;      // ACTIVE / INACTIVE / SUSPENDED
    private String registeredAt;
    private String lastSeenAt;
    private String metadataJson;

    public AuditSource() {}

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final AuditSource source = new AuditSource();
        public Builder id(String v) { source.id = v; return this; }
        public Builder name(String v) { source.name = v; return this; }
        public Builder type(String v) { source.type = v; return this; }
        public Builder version(String v) { source.version = v; return this; }
        public Builder tenant(String v) { source.tenant = v; return this; }
        public Builder description(String v) { source.description = v; return this; }
        public Builder endpoint(String v) { source.endpoint = v; return this; }
        public Builder authToken(String v) { source.authToken = v; return this; }
        public Builder status(String v) { source.status = v; return this; }
        public Builder registeredAt(String v) { source.registeredAt = v; return this; }
        public Builder lastSeenAt(String v) { source.lastSeenAt = v; return this; }
        public Builder metadataJson(String v) { source.metadataJson = v; return this; }
        public AuditSource build() { return source; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getTenant() { return tenant; }
    public void setTenant(String tenant) { this.tenant = tenant; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(String registeredAt) { this.registeredAt = registeredAt; }
    public String getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(String lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
}
