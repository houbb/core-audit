package com.github.houbb.core.audit.application.domain.context;

/**
 * 业务上下文 — 表示"在什么业务域"
 * <p>部分字段由 BusinessContextProvider 自动填充（module、environment），</p>
 * <p>部分字段由 ContextResolver 从 AuditEvent.metadata 桥接（workspace、project 等）。</p>
 */
public class BusinessContext {

    private String module;
    private String resource;
    private String resourceId;
    private String businessId;
    private String workspace;
    private String project;
    private String environment;
    /** P5: 会话 ID，由 ContextResolver 从 metadata 桥接或业务代码显式设置 */
    private String sessionId;
    /** P5: 工作流 ID，由 ContextResolver 从 metadata 桥接或业务代码显式设置 */
    private String workflowId;

    // ======== Constructors ========

    public BusinessContext() {
    }

    // ======== Getters & Setters ========

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getWorkspace() { return workspace; }
    public void setWorkspace(String workspace) { this.workspace = workspace; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
}
