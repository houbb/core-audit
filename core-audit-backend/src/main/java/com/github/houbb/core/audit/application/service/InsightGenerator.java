package com.github.houbb.core.audit.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.intelligence.AuditInsight;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 洞察生成器
 * <p>聚合 Rule Engine + Pattern Engine + AI Analyzer 的分析结果，生成结构化的 Insight。</p>
 *
 * <p>Generate: Title + Summary + Severity + Evidence + Suggestion</p>
 */
@Service
public class InsightGenerator {

    private static final Logger log = LoggerFactory.getLogger(InsightGenerator.class);

    private final ObjectMapper objectMapper;

    public InsightGenerator() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成洞察
     *
     * @param event 审计事件
     * @param ruleResult 规则引擎结果（可为 null）
     * @param patternAnomalies 模式异常发现（可为空列表）
     * @param aiResult AI 分析结果（可为 null）
     * @return Insight 洞察（可为 null = 无值得报告的发现）
     */
    public AuditInsight generate(AuditEvent event,
                                  RuleEngine.RuleResult ruleResult,
                                  List<PatternEngine.PatternAnomaly> patternAnomalies,
                                  AiAnalyzer.AiAnalysisResult aiResult) {

        // 决定严重程度
        RiskLevel severity = determineSeverity(ruleResult, patternAnomalies);

        // 如果没有任何发现，且严重程度为 LOW，不生成 Insight
        if (severity == RiskLevel.LOW && ruleResult == null && (patternAnomalies == null || patternAnomalies.isEmpty())) {
            return null; // 无值得报告的发现
        }

        // 生成标题
        String title = generateTitle(event, ruleResult, patternAnomalies);

        // 生成摘要
        String summary = buildSummary(event, ruleResult, patternAnomalies, aiResult);

        // 生成建议
        String suggestion = buildSuggestion(ruleResult, aiResult);

        // 生成证据 JSON
        String evidenceJson = buildEvidence(event);

        return AuditInsight.builder()
                .auditId(event.getId())
                .title(title)
                .severity(severity)
                .summary(summary)
                .suggestion(suggestion)
                .evidenceJson(evidenceJson)
                .agentName("InsightGenerator")
                .build();
    }

    /**
     * 生成全局建议（audit_id 为空的 Insight）
     */
    public AuditInsight generateGlobalRecommendation(String title, String suggestion, RiskLevel severity) {
        return AuditInsight.builder()
                .auditId(null)
                .title(title)
                .severity(severity != null ? severity : RiskLevel.MEDIUM)
                .summary(title)
                .suggestion(suggestion)
                .agentName("RecommendationEngine")
                .build();
    }

    /**
     * 决定严重程度
     */
    private RiskLevel determineSeverity(RuleEngine.RuleResult ruleResult,
                                         List<PatternEngine.PatternAnomaly> patternAnomalies) {
        // 规则命中取规则等级
        if (ruleResult != null) {
            return ruleResult.getLevel();
        }

        // 模式异常
        if (patternAnomalies != null && !patternAnomalies.isEmpty()) {
            // 高频异常 → HIGH
            boolean hasHighFreq = patternAnomalies.stream()
                    .anyMatch(a -> "high-frequency".equals(a.getAnomalyType()));
            if (hasHighFreq) return RiskLevel.HIGH;
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    /**
     * 生成标题
     */
    private String generateTitle(AuditEvent event, RuleEngine.RuleResult ruleResult,
                                  List<PatternEngine.PatternAnomaly> patternAnomalies) {
        if (ruleResult != null) {
            return switch (ruleResult.getRuleName()) {
                case "midnight-delete" -> "凌晨异常删除操作";
                case "bulk-delete" -> "批量删除风险";
                case "bulk-delete-medium" -> "批量删除警告";
                case "off-hours-admin-login" -> "非工作时间管理员登录";
                case "sensitive-module-first-access" -> "敏感模块首次访问";
                default -> "异常操作检测";
            };
        }

        if (patternAnomalies != null && !patternAnomalies.isEmpty()) {
            return patternAnomalies.get(0).getTitle();
        }

        return event.getModule() + " " + event.getAction() + " 操作分析";
    }

    /**
     * 构建摘要
     */
    private String buildSummary(AuditEvent event, RuleEngine.RuleResult ruleResult,
                                 List<PatternEngine.PatternAnomaly> patternAnomalies,
                                 AiAnalyzer.AiAnalysisResult aiResult) {
        StringBuilder sb = new StringBuilder();

        // 基本信息
        sb.append("操作人 ").append(event.getOperatorName() != null ? event.getOperatorName() : "未知");
        sb.append(" 在 ").append(event.getOccurredAt() != null ? event.getOccurredAt() : "未知时间");
        sb.append(" 执行了 ").append(event.getAction()).append(" 操作。");

        // 规则发现
        if (ruleResult != null) {
            sb.append("规则引擎发现：").append(ruleResult.getReason()).append("。");
        }

        // 模式异常
        if (patternAnomalies != null && !patternAnomalies.isEmpty()) {
            for (PatternEngine.PatternAnomaly anomaly : patternAnomalies) {
                sb.append(anomaly.getDescription()).append("。");
            }
        }

        // AI 分析
        if (aiResult != null && aiResult.getSummary() != null) {
            sb.append("AI 分析：").append(aiResult.getSummary());
        }

        return sb.toString();
    }

    /**
     * 构建建议
     */
    private String buildSuggestion(RuleEngine.RuleResult ruleResult, AiAnalyzer.AiAnalysisResult aiResult) {
        if (aiResult != null && aiResult.getSuggestion() != null) {
            return aiResult.getSuggestion();
        }
        return "建议审查该操作，确认是否正常；如有异常请及时处理。";
    }

    /**
     * 构建证据 JSON
     */
    private String buildEvidence(AuditEvent event) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("auditId", event.getId());
        evidence.put("module", event.getModule() != null ? event.getModule().name() : null);
        evidence.put("action", event.getAction() != null ? event.getAction().name() : null);
        evidence.put("operatorId", event.getOperatorId());
        evidence.put("operatorName", event.getOperatorName());
        evidence.put("targetType", event.getTargetType());
        evidence.put("targetId", event.getTargetId());
        evidence.put("occurredAt", event.getOccurredAt() != null ? event.getOccurredAt().toString() : null);

        try {
            return objectMapper.writeValueAsString(evidence);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}