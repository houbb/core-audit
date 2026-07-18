package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.api.response.AuditEventResponse;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.AuditEventPage;
import com.github.houbb.core.audit.application.domain.enterprise.AuditSource;
import com.github.houbb.core.audit.application.query.AuditEventQuery;
import com.github.houbb.core.audit.application.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * P9 Enterprise Gateway API — 企业审计平台统一入口
 * <p>提供 /api/audit/ 路由下的企业级能力，
 * 聚合 Dashboard + Sources + Providers + Subscriptions 概览。</p>
 */
@RestController
@RequestMapping("/api/v1/audit/enterprise")
@Tag(name = "Enterprise — Gateway", description = "P9 企业统一审计平台 API")
public class EnterpriseGatewayController {

    private final AuditEventService auditEventService;
    private final SourceService sourceService;
    private final MarketplaceService marketplaceService;
    private final WebhookService webhookService;
    private final TimelineService timelineService;

    public EnterpriseGatewayController(AuditEventService auditEventService,
                                        SourceService sourceService,
                                        MarketplaceService marketplaceService,
                                        WebhookService webhookService,
                                        TimelineService timelineService) {
        this.auditEventService = auditEventService;
        this.sourceService = sourceService;
        this.marketplaceService = marketplaceService;
        this.webhookService = webhookService;
        this.timelineService = timelineService;
    }

    @GetMapping("/overview")
    @Operation(summary = "企业平台总览")
    public ResponseEntity<Map<String, Object>> overview(
            @RequestParam(defaultValue = "default") String tenant) {

        AuditEventService.DashboardStats stats = auditEventService.getDashboardStats();

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("auditEvents", Map.of(
                "today", stats.getTodayTotal(),
                "success", stats.getTodaySuccess(),
                "fail", stats.getTodayFail(),
                "activeModules", stats.getActiveModules()
        ));
        overview.put("highRisk", Map.of(
                "critical", stats.getTodayCriticalCount(),
                "high", stats.getTodayHighCount(),
                "avgRiskScore", stats.getAvgRiskScore()
        ));
        overview.put("timeline", Map.of(
                "todayCount", stats.getTodayTimelineCount(),
                "avgLength", stats.getAvgTimelineLength(),
                "maxLength", stats.getMaxTimelineLength()
        ));
        overview.put("compliance", Map.of(
                "hashVerifyRate", stats.getHashVerifyRate(),
                "legalHoldCount", stats.getLegalHoldCount(),
                "retentionPolicyCount", stats.getRetentionPolicyCount()
        ));
        overview.put("enterprise", Map.of(
                "sourceCount", stats.getSourceCount(),
                "providerCount", stats.getProviderCount(),
                "subscriptionCount", stats.getSubscriptionCount(),
                "webhookDeliveryCount", stats.getWebhookDeliveryCount()
        ));

        // Recent events
        AuditEventQuery recentQuery = new AuditEventQuery();
        recentQuery.setPage(1);
        recentQuery.setSize(5);
        AuditEventPage recentPage = auditEventService.query(recentQuery);
        overview.put("recentEvents", recentPage.getItems().stream()
                .map(AuditEventResponse::from)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(overview);
    }

    @GetMapping("/health")
    @Operation(summary = "平台健康检查")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("sources", sourceService.countByTenant("default"));
        health.put("providers", marketplaceService.countByTenant("default"));
        health.put("subscriptions", webhookService.getEnabledSubscriptions().size());
        return ResponseEntity.ok(health);
    }
}