package com.github.houbb.core.audit.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.intelligence.*;
import com.github.houbb.core.audit.application.intelligence.AuditAgent;
import com.github.houbb.core.audit.application.port.*;
import com.github.houbb.core.audit.infrastructure.config.AuditProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 智能分析核心服务 — P8 Intelligence Runtime 中心调度器
 * <p>编排 Pattern → Risk → AI → Insight → Recommendation 流水线。
 * 在 AuditEventService.record() 中调用，采用 fault isolation 模式。</p>
 *
 * <p>流水线：</p>
 * <pre>
 * Audit Event
 *   → Pattern Analysis (模式识别)
 *   → Risk Analysis   (风险评估)
 *   → AI Reasoning    (AI 推理)
 *   → Insight         (生成洞察)
 *   → Recommendation  (生成建议)
 * </pre>
 */
@Service
public class IntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(IntelligenceService.class);

    private final AuditProperties auditProperties;
    private final RuleEngine ruleEngine;
    private final PatternEngine patternEngine;
    private final AiAnalyzer aiAnalyzer;
    private final InsightGenerator insightGenerator;
    private final RecommendationEngine recommendationEngine;
    private final AuditRiskRepository riskRepository;
    private final AuditInsightRepository insightRepository;
    private final List<AuditAgent> agents;
    private final ObjectMapper objectMapper;

    public IntelligenceService(AuditProperties auditProperties,
                                RuleEngine ruleEngine,
                                PatternEngine patternEngine,
                                AiAnalyzer aiAnalyzer,
                                InsightGenerator insightGenerator,
                                RecommendationEngine recommendationEngine,
                                AuditRiskRepository riskRepository,
                                AuditInsightRepository insightRepository,
                                List<AuditAgent> agents) {
        this.auditProperties = auditProperties;
        this.ruleEngine = ruleEngine;
        this.patternEngine = patternEngine;
        this.aiAnalyzer = aiAnalyzer;
        this.insightGenerator = insightGenerator;
        this.recommendationEngine = recommendationEngine;
        this.riskRepository = riskRepository;
        this.insightRepository = insightRepository;
        this.agents = agents != null ? agents : Collections.emptyList();
        this.agents.sort(Comparator.comparingInt(AuditAgent::order));
        this.objectMapper = new ObjectMapper();
        log.info("IntelligenceService initialized with {} agent(s)", this.agents.size());
    }

    /**
     * 分析审计事件
     * <p>在 AuditEventService.record() 中调用，fault-isolated。</p>
     *
     * @param event 已保存的审计事件
     */
    public void analyze(AuditEvent event) {
        if (event == null) return;

        AuditProperties.Intelligence intelligence = auditProperties.getIntelligence();
        if (intelligence == null || !intelligence.isEnabled()) {
            log.debug("Intelligence is disabled, skipping analysis");
            return;
        }

        log.debug("Analyzing audit event: id={}, module={}, action={}",
                event.getId(), event.getModule(), event.getAction());

        try {
            // Step 1: Pattern Analysis
            List<PatternEngine.PatternAnomaly> patternAnomalies = Collections.emptyList();
            try {
                patternAnomalies = patternEngine.detectAnomalies(event);
            } catch (Exception e) {
                log.warn("Pattern analysis failed for {}: {}", event.getId(), e.getMessage());
            }

            // Step 2: Risk Analysis (Rule Engine)
            RuleEngine.RuleResult ruleResult = null;
            try {
                ruleResult = ruleEngine.evaluate(event);
            } catch (Exception e) {
                log.warn("Rule evaluation failed for {}: {}", event.getId(), e.getMessage());
            }

            // Step 3: Determine risk score and level
            int riskScore = calculateRiskScore(ruleResult, patternAnomalies);
            RiskLevel riskLevel = RiskLevel.fromScore(riskScore);

            // Step 4: AI Reasoning
            AiAnalyzer.AiAnalysisResult aiResult = null;
            try {
                aiResult = aiAnalyzer.analyze(event, ruleResult);
            } catch (Exception e) {
                log.warn("AI analysis failed for {}: {}", event.getId(), e.getMessage());
            }

            // Step 5: Save Risk
            String reason = buildRiskReason(ruleResult, patternAnomalies);
            AuditRisk risk = AuditRisk.builder()
                    .auditId(event.getId())
                    .riskScore(riskScore)
                    .riskLevel(riskLevel)
                    .reason(reason)
                    .ruleName(ruleResult != null ? ruleResult.getRuleName() : null)
                    .aiAnalysis(aiResult != null ? aiResult.getSummary() : null)
                    .analyzedAt(LocalDateTime.now())
                    .build();
            riskRepository.save(risk);

            // Step 6: Generate Insight
            AuditInsight insight = insightGenerator.generate(event, ruleResult, patternAnomalies, aiResult);
            if (insight != null) {
                insightRepository.save(insight);
            }

            // Step 7: Run SPI Agents
            for (AuditAgent agent : agents) {
                try {
                    if (agent.supports(event)) {
                        AuditInsight agentInsight = agent.analyze(event);
                        if (agentInsight != null) {
                            insightRepository.save(agentInsight);
                            log.debug("Agent '{}' generated insight: {}", agent.getClass().getSimpleName(),
                                    agentInsight.getTitle());
                        }
                    }
                } catch (Exception e) {
                    log.warn("Agent '{}' failed for audit {}: {}", agent.getClass().getSimpleName(),
                            event.getId(), e.getMessage());
                }
            }

            log.debug("Intelligence analysis complete: id={}, riskScore={}, riskLevel={}",
                    event.getId(), riskScore, riskLevel);

        } catch (Exception e) {
            log.warn("Intelligence analysis failed for audit {}: {}", event.getId(), e.getMessage());
            // Fault isolation — do NOT rethrow
        }
    }

    /**
     * 计算综合风险评分
     */
    private int calculateRiskScore(RuleEngine.RuleResult ruleResult,
                                    List<PatternEngine.PatternAnomaly> patternAnomalies) {
        int score = 0;

        // 规则命中取规则评分
        if (ruleResult != null) {
            score = Math.max(score, ruleResult.getScore());
        }

        // 模式异常加分
        if (patternAnomalies != null && !patternAnomalies.isEmpty()) {
            for (PatternEngine.PatternAnomaly anomaly : patternAnomalies) {
                score += switch (anomaly.getAnomalyType()) {
                    case "high-frequency" -> 20;
                    case "time-anomaly" -> 10;
                    case "action-distribution" -> 15;
                    default -> 5;
                };
            }
        }

        // 默认常规操作
        if (score == 0) {
            score = 5; // 常规操作低分
        }

        return Math.min(100, score);
    }

    /**
     * 构建风险原因文本
     */
    private String buildRiskReason(RuleEngine.RuleResult ruleResult,
                                    List<PatternEngine.PatternAnomaly> patternAnomalies) {
        StringBuilder sb = new StringBuilder();

        if (ruleResult != null) {
            sb.append(ruleResult.getReason());
        }

        if (patternAnomalies != null && !patternAnomalies.isEmpty()) {
            if (sb.length() > 0) sb.append("；");
            for (PatternEngine.PatternAnomaly anomaly : patternAnomalies) {
                sb.append(anomaly.getDescription()).append("；");
            }
        }

        if (sb.isEmpty()) {
            sb.append("常规操作，未检测到异常");
        }

        return sb.toString();
    }

    // ======== Query Methods ========

    /** 获取 Insight 列表 */
    public List<AuditInsight> getInsights(int page, int size) {
        return insightRepository.findAll(page, size);
    }

    /** 获取单个 Insight */
    public Optional<AuditInsight> getInsight(String id) {
        return insightRepository.findById(id);
    }

    /** 获取指定 Audit 的风险评分 */
    public Optional<AuditRisk> getRisk(String auditId) {
        return riskRepository.findByAuditId(auditId);
    }

    /** 获取全局建议列表 */
    public List<AuditInsight> getRecommendations(int limit) {
        return recommendationEngine.getRecommendations(limit);
    }

    /** 获取 Intelligence Dashboard 统计 */
    public IntelligenceDashboardStats getDashboardStats() {
        IntelligenceDashboardStats stats = new IntelligenceDashboardStats();
        stats.setTodayInsightCount(insightRepository.countToday());
        stats.setTodayCriticalCount(riskRepository.countTodayByLevel(RiskLevel.CRITICAL));
        stats.setTodayHighCount(riskRepository.countTodayByLevel(RiskLevel.HIGH));
        stats.setTodayMediumCount(riskRepository.countTodayByLevel(RiskLevel.MEDIUM));
        stats.setAvgRiskScore(riskRepository.avgRiskScoreToday());
        return stats;
    }

    /**
     * Intelligence Dashboard 统计数据
     */
    public static class IntelligenceDashboardStats {
        private long todayInsightCount;
        private long todayCriticalCount;
        private long todayHighCount;
        private long todayMediumCount;
        private double avgRiskScore;

        public long getTodayInsightCount() { return todayInsightCount; }
        public void setTodayInsightCount(long todayInsightCount) { this.todayInsightCount = todayInsightCount; }
        public long getTodayCriticalCount() { return todayCriticalCount; }
        public void setTodayCriticalCount(long todayCriticalCount) { this.todayCriticalCount = todayCriticalCount; }
        public long getTodayHighCount() { return todayHighCount; }
        public void setTodayHighCount(long todayHighCount) { this.todayHighCount = todayHighCount; }
        public long getTodayMediumCount() { return todayMediumCount; }
        public void setTodayMediumCount(long todayMediumCount) { this.todayMediumCount = todayMediumCount; }
        public double getAvgRiskScore() { return avgRiskScore; }
        public void setAvgRiskScore(double avgRiskScore) { this.avgRiskScore = avgRiskScore; }
    }
}