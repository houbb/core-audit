package com.github.houbb.core.audit.application.query;

import com.github.houbb.core.audit.application.domain.enums.AuditAction;
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

    // ======== Getters & Setters ========

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = Math.min(size, 100); }
    public AuditModule getModule() { return module; }
    public void setModule(AuditModule module) { this.module = module; }
    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }
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
}