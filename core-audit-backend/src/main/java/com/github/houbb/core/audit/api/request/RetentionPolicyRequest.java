package com.github.houbb.core.audit.api.request;

/**
 * 保留策略请求
 */
public class RetentionPolicyRequest {

    private String module;
    private String action;   // null = 该模块所有 action
    private int retentionDays = 365;
    private boolean archive;
    private boolean enabled = true;

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public int getRetentionDays() { return retentionDays; }
    public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }
    public boolean isArchive() { return archive; }
    public void setArchive(boolean archive) { this.archive = archive; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}