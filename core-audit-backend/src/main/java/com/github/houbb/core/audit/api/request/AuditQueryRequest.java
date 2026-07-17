package com.github.houbb.core.audit.api.request;

import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

/**
 * 审计查询请求 DTO
 * <p>字段与 AuditQuery DSL 一一对应。</p>
 */
public class AuditQueryRequest {

    // ======== 时间范围 ========
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // ======== 基础过滤 ========
    private AuditModule module;
    private AuditAction action;
    private AuditResult result;
    private AuditEventType eventType;
    private String targetType;
    private String targetId;

    // ======== 操作人 ========
    private String operator;
    private String tenant;
    private String department;
    private String role;

    // ======== 请求信息 ========
    private String ip;
    private String uri;
    private String requestMethod;
    private String browser;
    private String os;
    private String device;
    private String traceId;

    // ======== Diff 过滤 ========
    private String diffField;
    private String diffBefore;
    private String diffAfter;
    private ChangeType diffType;

    // ======== 全文搜索 ========
    private String keyword;

    // ======== Metadata ========
    private String metadataKey;
    private String metadataValue;

    // ======== 排序 ========
    private String sortField;
    private String sortDirection;

    // ======== 分页 ========
    @Min(value = 1, message = "page must be >= 1")
    private int page = 1;

    @Min(value = 1, message = "size must be >= 1")
    @Max(value = 100, message = "size must be <= 100")
    private int size = 20;

    // ======== Getters & Setters ========

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public AuditModule getModule() { return module; }
    public void setModule(AuditModule module) { this.module = module; }
    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }
    public AuditResult getResult() { return result; }
    public void setResult(AuditResult result) { this.result = result; }
    public AuditEventType getEventType() { return eventType; }
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getTenant() { return tenant; }
    public void setTenant(String tenant) { this.tenant = tenant; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getDiffField() { return diffField; }
    public void setDiffField(String diffField) { this.diffField = diffField; }
    public String getDiffBefore() { return diffBefore; }
    public void setDiffBefore(String diffBefore) { this.diffBefore = diffBefore; }
    public String getDiffAfter() { return diffAfter; }
    public void setDiffAfter(String diffAfter) { this.diffAfter = diffAfter; }
    public ChangeType getDiffType() { return diffType; }
    public void setDiffType(ChangeType diffType) { this.diffType = diffType; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getMetadataKey() { return metadataKey; }
    public void setMetadataKey(String metadataKey) { this.metadataKey = metadataKey; }
    public String getMetadataValue() { return metadataValue; }
    public void setMetadataValue(String metadataValue) { this.metadataValue = metadataValue; }
    public String getSortField() { return sortField; }
    public void setSortField(String sortField) { this.sortField = sortField; }
    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}