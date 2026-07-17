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

    // ======== P3 diff stats ========
    private Map<String, Long> changeTypeDistribution;
    private List<Map<String, Object>> topChangedFields;

    // ======== P5 timeline stats ========
    private long todayTimelineCount;
    private double avgTimelineLength;
    private int maxTimelineLength;
    private double avgTimelineDuration;

    // ======== P6 replay stats ========
    private long todayReplayCount;
    private double avgReplaySteps;
    private int maxReplaySteps;
    private double avgReplayDuration;

    // ======== P7 compliance stats ========
    private double hashVerifyRate;
    private long legalHoldCount;
    private long retentionPolicyCount;
    private long todayExportCount;

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
    public Map<String, Long> getChangeTypeDistribution() { return changeTypeDistribution; }
    public List<Map<String, Object>> getTopChangedFields() { return topChangedFields; }
    public long getTodayTimelineCount() { return todayTimelineCount; }
    public double getAvgTimelineLength() { return avgTimelineLength; }
    public int getMaxTimelineLength() { return maxTimelineLength; }
    public double getAvgTimelineDuration() { return avgTimelineDuration; }
    public long getTodayReplayCount() { return todayReplayCount; }
    public double getAvgReplaySteps() { return avgReplaySteps; }
    public int getMaxReplaySteps() { return maxReplaySteps; }
    public double getAvgReplayDuration() { return avgReplayDuration; }

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
    public DashboardResponse changeTypeDistribution(Map<String, Long> v) { this.changeTypeDistribution = v; return this; }
    public DashboardResponse topChangedFields(List<Map<String, Object>> v) { this.topChangedFields = v; return this; }
    public DashboardResponse todayTimelineCount(long v) { this.todayTimelineCount = v; return this; }
    public DashboardResponse avgTimelineLength(double v) { this.avgTimelineLength = v; return this; }
    public DashboardResponse maxTimelineLength(int v) { this.maxTimelineLength = v; return this; }
    public DashboardResponse avgTimelineDuration(double v) { this.avgTimelineDuration = v; return this; }
    public DashboardResponse todayReplayCount(long v) { this.todayReplayCount = v; return this; }
    public DashboardResponse avgReplaySteps(double v) { this.avgReplaySteps = v; return this; }
    public DashboardResponse maxReplaySteps(int v) { this.maxReplaySteps = v; return this; }
    public DashboardResponse avgReplayDuration(double v) { this.avgReplayDuration = v; return this; }
    public double getHashVerifyRate() { return hashVerifyRate; }
    public DashboardResponse hashVerifyRate(double v) { this.hashVerifyRate = v; return this; }
    public long getLegalHoldCount() { return legalHoldCount; }
    public DashboardResponse legalHoldCount(long v) { this.legalHoldCount = v; return this; }
    public long getRetentionPolicyCount() { return retentionPolicyCount; }
    public DashboardResponse retentionPolicyCount(long v) { this.retentionPolicyCount = v; return this; }
    public long getTodayExportCount() { return todayExportCount; }
    public DashboardResponse todayExportCount(long v) { this.todayExportCount = v; return this; }
}