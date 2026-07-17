package com.github.houbb.core.audit.application.domain.compliance;

import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;

/**
 * 审计数据保留策略
 * <p>按 Module + Action 定义审计数据的保留天数。</p>
 */
public class RetentionPolicy {

    private String id;
    private AuditModule module;
    private AuditAction action;    // null = 该模块所有 action
    private int retentionDays;
    private boolean archive;
    private boolean enabled;

    public RetentionPolicy() {
    }

    private RetentionPolicy(Builder builder) {
        this.id = builder.id;
        this.module = builder.module;
        this.action = builder.action;
        this.retentionDays = builder.retentionDays;
        this.archive = builder.archive;
        this.enabled = builder.enabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private AuditModule module;
        private AuditAction action;
        private int retentionDays = 365;
        private boolean archive;
        private boolean enabled = true;

        public Builder id(String id) { this.id = id; return this; }
        public Builder module(AuditModule module) { this.module = module; return this; }
        public Builder action(AuditAction action) { this.action = action; return this; }
        public Builder retentionDays(int retentionDays) { this.retentionDays = retentionDays; return this; }
        public Builder archive(boolean archive) { this.archive = archive; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }

        public RetentionPolicy build() {
            return new RetentionPolicy(this);
        }
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public AuditModule getModule() { return module; }
    public void setModule(AuditModule module) { this.module = module; }
    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }
    public int getRetentionDays() { return retentionDays; }
    public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }
    public boolean isArchive() { return archive; }
    public void setArchive(boolean archive) { this.archive = archive; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}