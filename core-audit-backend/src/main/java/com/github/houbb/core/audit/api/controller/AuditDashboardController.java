package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.api.response.AuditEventResponse;
import com.github.houbb.core.audit.api.response.DashboardResponse;
import com.github.houbb.core.audit.application.event.EventBus;
import com.github.houbb.core.audit.application.service.AuditEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard API
 * <p>提供审计概览统计数据。</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Dashboard", description = "审计 Dashboard API")
public class AuditDashboardController {

    private final AuditEventService auditEventService;
    private final EventBus eventBus;

    public AuditDashboardController(AuditEventService auditEventService, EventBus eventBus) {
        this.auditEventService = auditEventService;
        this.eventBus = eventBus;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard 统计", description = "返回今天审计总数、成功数、失败数、活跃模块数及最近操作")
    public ResponseEntity<DashboardResponse> getDashboard() {
        AuditEventService.DashboardStats stats = auditEventService.getDashboardStats();
        DashboardResponse response = new DashboardResponse()
                .todayTotal(stats.getTodayTotal())
                .todaySuccess(stats.getTodaySuccess())
                .todayFail(stats.getTodayFail())
                .activeModules(stats.getActiveModules())
                .todayPublished(stats.getTodayPublished())
                .todayPublishFailed(stats.getTodayPublishFailed())
                .subscriberCount(eventBus.getSubscribers().size())
                .browserDistribution(stats.getBrowserDistribution())
                .topOperators(stats.getTopOperators())
                .topModules(stats.getTopModules())
                .topOrganizations(stats.getTopOrganizations())
                .changeTypeDistribution(stats.getChangeTypeDistribution())
                .topChangedFields(stats.getTopChangedFields())
                .todayTimelineCount(stats.getTodayTimelineCount())
                .avgTimelineLength(stats.getAvgTimelineLength())
                .maxTimelineLength(stats.getMaxTimelineLength())
                .avgTimelineDuration(stats.getAvgTimelineDuration())
                .todayReplayCount(stats.getTodayReplayCount())
                .avgReplaySteps(stats.getAvgReplaySteps())
                .maxReplaySteps(stats.getMaxReplaySteps())
                .avgReplayDuration(stats.getAvgReplayDuration())
                .hashVerifyRate(stats.getHashVerifyRate())
                .legalHoldCount(stats.getLegalHoldCount())
                .retentionPolicyCount(stats.getRetentionPolicyCount())
                .todayExportCount(stats.getTodayExportCount())
                .todayInsightCount(stats.getTodayInsightCount())
                .todayCriticalCount(stats.getTodayCriticalCount())
                .todayHighCount(stats.getTodayHighCount())
                .avgRiskScore(stats.getAvgRiskScore())
                .recentEvents(stats.getRecentEvents().stream()
                        .map(AuditEventResponse::from)
                        .toList());
        return ResponseEntity.ok(response);
    }
}