package com.github.houbb.core.audit.application.domain.query;

import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditEventType;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.AuditResult;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;

import java.time.LocalDateTime;

/**
 * 统一审计查询 DSL（Domain Query Language）
 * <p>P4 Search Runtime 的核心查询模型。所有查询条件统一用此对象描述，
 * 替代 Controller 层散落的 @RequestParam。</p>
 *
 * <p>使用方式：</p>
 * <pre>{@code
 * AuditQuery query = AuditQuery.builder()
 *     .module(AuditModule.USER)
 *     .action(AuditAction.DELETE)
 *     .operator("echo")
 *     .startTime(yesterday)
 *     .endTime(today)
 *     .sortField("created_at")
 *     .sortDirection("DESC")
 *     .page(1)
 *     .size(20)
 *     .build();
 * }</pre>
 */
public class AuditQuery {

    // ======== 时间范围 ========

    /** 开始时间（含） */
    private LocalDateTime startTime;

    /** 结束时间（含） */
    private LocalDateTime endTime;

    // ======== 基础过滤（Basic Scope）========

    /** 模块过滤 */
    private AuditModule module;

    /** 操作过滤 */
    private AuditAction action;

    /** 结果过滤 */
    private AuditResult result;

    /** 事件类型过滤 */
    private AuditEventType eventType;

    /** 目标对象类型 */
    private String targetType;

    /** 目标对象 ID */
    private String targetId;

    // ======== 操作人（Context Scope — Operator）========

    /** 操作人名称（模糊匹配） */
    private String operator;

    /** 租户过滤 */
    private String tenant;

    /** 部门过滤 */
    private String department;

    /** 角色过滤 */
    private String role;

    // ======== 请求信息（Context Scope — Request/Client）========

    /** 客户端 IP */
    private String ip;

    /** 请求 URI */
    private String uri;

    /** 请求方法 */
    private String requestMethod;

    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 设备 */
    private String device;

    /** 分布式 Trace ID */
    private String traceId;

    // ======== Diff 过滤（Diff Scope）========

    /** 变更字段名 */
    private String diffField;

    /** 变更前值（模糊匹配） */
    private String diffBefore;

    /** 变更后值（模糊匹配） */
    private String diffAfter;

    /** 变更类型 */
    private ChangeType diffType;

    // ======== 全文搜索（Keyword Scope）========

    /** 关键字搜索（匹配 description + targetId + operatorName） */
    private String keyword;

    // ======== Metadata 过滤（Metadata Scope）========

    /** Metadata JSON key */
    private String metadataKey;

    /** Metadata JSON value（模糊匹配） */
    private String metadataValue;

    // ======== 排序 ========

    /** 排序字段：created_at / operator_name / module */
    private String sortField;

    /** 排序方向：ASC / DESC */
    private String sortDirection;

    // ======== 分页 ========

    /** 页码（从 1 开始） */
    private int page = 1;

    /** 每页条数（最大 100） */
    private int size = 20;

    // ======== Builder ========

    public static Builder builder() {
        return new Builder();
    }

    private AuditQuery() {
    }

    public static final class Builder {
        private final AuditQuery query = new AuditQuery();

        public Builder startTime(LocalDateTime startTime) { query.startTime = startTime; return this; }
        public Builder endTime(LocalDateTime endTime) { query.endTime = endTime; return this; }
        public Builder module(AuditModule module) { query.module = module; return this; }
        public Builder action(AuditAction action) { query.action = action; return this; }
        public Builder result(AuditResult result) { query.result = result; return this; }
        public Builder eventType(AuditEventType eventType) { query.eventType = eventType; return this; }
        public Builder targetType(String targetType) { query.targetType = targetType; return this; }
        public Builder targetId(String targetId) { query.targetId = targetId; return this; }
        public Builder operator(String operator) { query.operator = operator; return this; }
        public Builder tenant(String tenant) { query.tenant = tenant; return this; }
        public Builder department(String department) { query.department = department; return this; }
        public Builder role(String role) { query.role = role; return this; }
        public Builder ip(String ip) { query.ip = ip; return this; }
        public Builder uri(String uri) { query.uri = uri; return this; }
        public Builder requestMethod(String requestMethod) { query.requestMethod = requestMethod; return this; }
        public Builder browser(String browser) { query.browser = browser; return this; }
        public Builder os(String os) { query.os = os; return this; }
        public Builder device(String device) { query.device = device; return this; }
        public Builder traceId(String traceId) { query.traceId = traceId; return this; }
        public Builder diffField(String diffField) { query.diffField = diffField; return this; }
        public Builder diffBefore(String diffBefore) { query.diffBefore = diffBefore; return this; }
        public Builder diffAfter(String diffAfter) { query.diffAfter = diffAfter; return this; }
        public Builder diffType(ChangeType diffType) { query.diffType = diffType; return this; }
        public Builder keyword(String keyword) { query.keyword = keyword; return this; }
        public Builder metadataKey(String metadataKey) { query.metadataKey = metadataKey; return this; }
        public Builder metadataValue(String metadataValue) { query.metadataValue = metadataValue; return this; }
        public Builder sortField(String sortField) { query.sortField = sortField; return this; }
        public Builder sortDirection(String sortDirection) { query.sortDirection = sortDirection; return this; }
        public Builder page(int page) { query.page = Math.max(page, 1); return this; }
        public Builder size(int size) { query.size = Math.min(Math.max(size, 1), 100); return this; }

        public AuditQuery build() {
            return query;
        }
    }

    // ======== Getters ========

    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public AuditModule getModule() { return module; }
    public AuditAction getAction() { return action; }
    public AuditResult getResult() { return result; }
    public AuditEventType getEventType() { return eventType; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getOperator() { return operator; }
    public String getTenant() { return tenant; }
    public String getDepartment() { return department; }
    public String getRole() { return role; }
    public String getIp() { return ip; }
    public String getUri() { return uri; }
    public String getRequestMethod() { return requestMethod; }
    public String getBrowser() { return browser; }
    public String getOs() { return os; }
    public String getDevice() { return device; }
    public String getTraceId() { return traceId; }
    public String getDiffField() { return diffField; }
    public String getDiffBefore() { return diffBefore; }
    public String getDiffAfter() { return diffAfter; }
    public ChangeType getDiffType() { return diffType; }
    public String getKeyword() { return keyword; }
    public String getMetadataKey() { return metadataKey; }
    public String getMetadataValue() { return metadataValue; }
    public String getSortField() { return sortField; }
    public String getSortDirection() { return sortDirection; }
    public int getPage() { return page; }
    public int getSize() { return size; }

    // ======== Setters（用于 Jackson 反序列化）========

    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setModule(AuditModule module) { this.module = module; }
    public void setAction(AuditAction action) { this.action = action; }
    public void setResult(AuditResult result) { this.result = result; }
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public void setOperator(String operator) { this.operator = operator; }
    public void setTenant(String tenant) { this.tenant = tenant; }
    public void setDepartment(String department) { this.department = department; }
    public void setRole(String role) { this.role = role; }
    public void setIp(String ip) { this.ip = ip; }
    public void setUri(String uri) { this.uri = uri; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public void setBrowser(String browser) { this.browser = browser; }
    public void setOs(String os) { this.os = os; }
    public void setDevice(String device) { this.device = device; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public void setDiffField(String diffField) { this.diffField = diffField; }
    public void setDiffBefore(String diffBefore) { this.diffBefore = diffBefore; }
    public void setDiffAfter(String diffAfter) { this.diffAfter = diffAfter; }
    public void setDiffType(ChangeType diffType) { this.diffType = diffType; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public void setMetadataKey(String metadataKey) { this.metadataKey = metadataKey; }
    public void setMetadataValue(String metadataValue) { this.metadataValue = metadataValue; }
    public void setSortField(String sortField) { this.sortField = sortField; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
    public void setPage(int page) { this.page = Math.max(page, 1); }
    public void setSize(int size) { this.size = Math.min(Math.max(size, 1), 100); }

    /**
     * 检查是否有 Diff 过滤条件
     */
    public boolean hasDiffFilter() {
        return (diffField != null && !diffField.isBlank())
                || (diffBefore != null && !diffBefore.isBlank())
                || (diffAfter != null && !diffAfter.isBlank())
                || diffType != null;
    }
}