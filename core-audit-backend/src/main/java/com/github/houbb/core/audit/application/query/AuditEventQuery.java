package com.github.houbb.core.audit.application.query;

import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;

import java.time.LocalDateTime;

/**
 * 审计事件查询条件
 */
public class AuditEventQuery {

    /** 页码（从 1 开始） */
    private int page = 1;

    /** 每页条数（最大 100） */
    private int size = 20;

    /** 模块过滤 */
    private AuditModule module;

    /** 操作过滤 */
    private AuditAction action;

    /** 事件类型过滤（P1 新增） */
    private AuditEventType eventType;

    /** 操作人过滤 */
    private String operator;

    /** 结果过滤 */
    private AuditResult result;

    /** 关键字搜索（description + targetId + operatorName） */
    private String keyword;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** Trace ID 过滤（P2 — 复用已有的 trace_id 列） */
    private String traceId;

    // ======== P2 context filter fields ========

    /** 租户过滤 */
    private String tenant;

    /** 部门过滤 */
    private String department;

    /** 浏览器过滤 */
    private String browser;

    /** IP 过滤 */
    private String ip;

    /** 工作空间过滤 */
    private String workspace;

    /** 项目过滤 */
    private String project;

    // ======== Getters & Setters ========

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = Math.min(size, 100); }
    public AuditModule getModule() { return module; }
    public void setModule(AuditModule module) { this.module = module; }
    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }
    public AuditEventType getEventType() { return eventType; }
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public AuditResult getResult() { return result; }
    public void setResult(AuditResult result) { this.result = result; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    // ======== P2 getters & setters ========

    public String getTenant() { return tenant; }
    public void setTenant(String tenant) { this.tenant = tenant; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getWorkspace() { return workspace; }
    public void setWorkspace(String workspace) { this.workspace = workspace; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
}