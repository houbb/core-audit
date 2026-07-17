package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.api.response.AuditEventResponse;
import com.github.houbb.core.audit.application.domain.intelligence.AuditInsight;
import com.github.houbb.core.audit.application.domain.intelligence.AuditRisk;
import com.github.houbb.core.audit.application.service.IntelligenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Intelligence API — P8 Intelligence Runtime
 * <p>提供 AI 风险分析、洞察查询、建议推荐等智能分析 REST 接口。</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Intelligence", description = "智能分析 API")
public class IntelligenceController {

    private final IntelligenceService intelligenceService;

    public IntelligenceController(IntelligenceService intelligenceService) {
        this.intelligenceService = intelligenceService;
    }

    @GetMapping("/intelligence/dashboard")
    @Operation(summary = "Intelligence Dashboard 统计")
    public ResponseEntity<IntelligenceDashboardResponse> getDashboard() {
        IntelligenceService.IntelligenceDashboardStats stats = intelligenceService.getDashboardStats();
        IntelligenceDashboardResponse resp = new IntelligenceDashboardResponse(
                stats.getTodayInsightCount(),
                stats.getTodayCriticalCount(),
                stats.getTodayHighCount(),
                stats.getTodayMediumCount(),
                stats.getAvgRiskScore()
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/intelligence/insights")
    @Operation(summary = "分页查询 Insights")
    public ResponseEntity<List<InsightResponse>> listInsights(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<AuditInsight> insights = intelligenceService.getInsights(page, size);
        List<InsightResponse> list = insights.stream().map(InsightResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/intelligence/insights/{id}")
    @Operation(summary = "获取单个 Insight 详情")
    public ResponseEntity<InsightResponse> getInsight(@PathVariable String id) {
        Optional<AuditInsight> insightOpt = intelligenceService.getInsight(id);
        return insightOpt.map(insight -> ResponseEntity.ok(InsightResponse.from(insight)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/intelligence/risk/{auditId}")
    @Operation(summary = "获取指定 Audit 的风险评分")
    public ResponseEntity<RiskResponse> getRisk(@PathVariable String auditId) {
        Optional<AuditRisk> riskOpt = intelligenceService.getRisk(auditId);
        return riskOpt.map(risk -> ResponseEntity.ok(RiskResponse.from(risk)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/intelligence/analyze")
    @Operation(summary = "手动触发分析")
    public ResponseEntity<String> triggerAnalyze(@RequestParam(required = false) String auditId) {
        // Manual trigger — full re-scan is done via the record() pipeline.
        // This endpoint is a placeholder for future manual re-analysis capabilities.
        return ResponseEntity.ok("Analysis pipeline is active. Events are analyzed automatically on record.");
    }

    @GetMapping("/intelligence/recommendations")
    @Operation(summary = "获取全局建议列表")
    public ResponseEntity<List<InsightResponse>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        List<AuditInsight> recommendations = intelligenceService.getRecommendations(limit);
        List<InsightResponse> list = recommendations.stream().map(InsightResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    /**
     * Insight 响应 DTO
     */
    public static class InsightResponse {
        private String id;
        private String auditId;
        private String title;
        private String severity;
        private String summary;
        private String suggestion;
        private String evidenceJson;
        private String agentName;

        public static InsightResponse from(AuditInsight insight) {
            InsightResponse r = new InsightResponse();
            r.id = insight.getId();
            r.auditId = insight.getAuditId();
            r.title = insight.getTitle();
            r.severity = insight.getSeverity() != null ? insight.getSeverity().name() : null;
            r.summary = insight.getSummary();
            r.suggestion = insight.getSuggestion();
            r.evidenceJson = insight.getEvidenceJson();
            r.agentName = insight.getAgentName();
            return r;
        }

        public String getId() { return id; }
        public String getAuditId() { return auditId; }
        public String getTitle() { return title; }
        public String getSeverity() { return severity; }
        public String getSummary() { return summary; }
        public String getSuggestion() { return suggestion; }
        public String getEvidenceJson() { return evidenceJson; }
        public String getAgentName() { return agentName; }
    }

    /**
     * Risk 响应 DTO
     */
    public static class RiskResponse {
        private String auditId;
        private int riskScore;
        private String riskLevel;
        private String reason;
        private String ruleName;
        private String aiAnalysis;
        private String analyzedAt;

        public static RiskResponse from(AuditRisk risk) {
            RiskResponse r = new RiskResponse();
            r.auditId = risk.getAuditId();
            r.riskScore = risk.getRiskScore();
            r.riskLevel = risk.getRiskLevel() != null ? risk.getRiskLevel().name() : null;
            r.reason = risk.getReason();
            r.ruleName = risk.getRuleName();
            r.aiAnalysis = risk.getAiAnalysis();
            r.analyzedAt = risk.getAnalyzedAt() != null ? risk.getAnalyzedAt().toString() : null;
            return r;
        }

        public String getAuditId() { return auditId; }
        public int getRiskScore() { return riskScore; }
        public String getRiskLevel() { return riskLevel; }
        public String getReason() { return reason; }
        public String getRuleName() { return ruleName; }
        public String getAiAnalysis() { return aiAnalysis; }
        public String getAnalyzedAt() { return analyzedAt; }
    }

    /**
     * Intelligence Dashboard 响应 DTO
     */
    public static class IntelligenceDashboardResponse {
        private long todayInsightCount;
        private long todayCriticalCount;
        private long todayHighCount;
        private long todayMediumCount;
        private double avgRiskScore;

        public IntelligenceDashboardResponse(long todayInsightCount, long todayCriticalCount,
                                              long todayHighCount, long todayMediumCount, double avgRiskScore) {
            this.todayInsightCount = todayInsightCount;
            this.todayCriticalCount = todayCriticalCount;
            this.todayHighCount = todayHighCount;
            this.todayMediumCount = todayMediumCount;
            this.avgRiskScore = avgRiskScore;
        }

        public long getTodayInsightCount() { return todayInsightCount; }
        public long getTodayCriticalCount() { return todayCriticalCount; }
        public long getTodayHighCount() { return todayHighCount; }
        public long getTodayMediumCount() { return todayMediumCount; }
        public double getAvgRiskScore() { return avgRiskScore; }
    }
}