package com.github.houbb.core.audit.api.response;

import java.util.List;
import java.util.Map;

/**
 * Dashboard 统计响应
 */
public class DashboardResponse {

    private long todayTotal;
    private long todaySuccess;
    private long todayFail;
    private int activeModules;
    private long todayPublished;
    private long todayPublishFailed;
    private int subscriberCount;
    private List<AuditEventResponse> recentEvents;

    // ======== P2 context stats ========
    private Map<String, Long> browserDistribution;
    private List<Map<String, Object>> topOperators;
    private List<Map<String, Object>> topModules;
    private List<Map<String, Object>> topOrganizations;

    // ======== Getters ========

    public long getTodayTotal() { return todayTotal; }
    public long getTodaySuccess() { return todaySuccess; }
    public long getTodayFail() { return todayFail; }
    public int getActiveModules() { return activeModules; }
    public long getTodayPublished() { return todayPublished; }
    public long getTodayPublishFailed() { return todayPublishFailed; }
    public int getSubscriberCount() { return subscriberCount; }
    public List<AuditEventResponse> getRecentEvents() { return recentEvents; }
    public Map<String, Long> getBrowserDistribution() { return browserDistribution; }
    public List<Map<String, Object>> getTopOperators() { return topOperators; }
    public List<Map<String, Object>> getTopModules() { return topModules; }
    public List<Map<String, Object>> getTopOrganizations() { return topOrganizations; }

    // ======== Builder-style setters ========

    public DashboardResponse todayTotal(long v) { this.todayTotal = v; return this; }
    public DashboardResponse todaySuccess(long v) { this.todaySuccess = v; return this; }
    public DashboardResponse todayFail(long v) { this.todayFail = v; return this; }
    public DashboardResponse activeModules(int v) { this.activeModules = v; return this; }
    public DashboardResponse todayPublished(long v) { this.todayPublished = v; return this; }
    public DashboardResponse todayPublishFailed(long v) { this.todayPublishFailed = v; return this; }
    public DashboardResponse subscriberCount(int v) { this.subscriberCount = v; return this; }
    public DashboardResponse recentEvents(List<AuditEventResponse> v) { this.recentEvents = v; return this; }
    public DashboardResponse browserDistribution(Map<String, Long> v) { this.browserDistribution = v; return this; }
    public DashboardResponse topOperators(List<Map<String, Object>> v) { this.topOperators = v; return this; }
    public DashboardResponse topModules(List<Map<String, Object>> v) { this.topModules = v; return this; }
    public DashboardResponse topOrganizations(List<Map<String, Object>> v) { this.topOrganizations = v; return this; }
}