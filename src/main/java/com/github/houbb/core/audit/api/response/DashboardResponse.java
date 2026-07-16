package com.github.houbb.core.audit.api.response;

import java.util.List;

/**
 * Dashboard 统计响应
 */
public class DashboardResponse {

    private long todayTotal;
    private long todaySuccess;
    private long todayFail;
    private int activeModules;
    private List<AuditEventResponse> recentEvents;

    // ======== Getters ========

    public long getTodayTotal() { return todayTotal; }
    public long getTodaySuccess() { return todaySuccess; }
    public long getTodayFail() { return todayFail; }
    public int getActiveModules() { return activeModules; }
    public List<AuditEventResponse> getRecentEvents() { return recentEvents; }

    // ======== Builder-style setters ========

    public DashboardResponse todayTotal(long v) { this.todayTotal = v; return this; }
    public DashboardResponse todaySuccess(long v) { this.todaySuccess = v; return this; }
    public DashboardResponse todayFail(long v) { this.todayFail = v; return this; }
    public DashboardResponse activeModules(int v) { this.activeModules = v; return this; }
    public DashboardResponse recentEvents(List<AuditEventResponse> v) { this.recentEvents = v; return this; }
}