package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.compliance.RetentionPolicy;

/**
 * 保留策略响应
 */
public class RetentionPolicyResponse {

    private String id;
    private String module;
    private String action;
    private int retentionDays;
    private boolean archive;
    private boolean enabled;

    public static RetentionPolicyResponse from(RetentionPolicy policy) {
        RetentionPolicyResponse resp = new RetentionPolicyResponse();
        resp.id = policy.getId();
        resp.module = policy.getModule() != null ? policy.getModule().name() : null;
        resp.action = policy.getAction() != null ? policy.getAction().name() : null;
        resp.retentionDays = policy.getRetentionDays();
        resp.archive = policy.isArchive();
        resp.enabled = policy.isEnabled();
        return resp;
    }

    public String getId() { return id; }
    public String getModule() { return module; }
    public String getAction() { return action; }
    public int getRetentionDays() { return retentionDays; }
    public boolean isArchive() { return archive; }
    public boolean isEnabled() { return enabled; }
}