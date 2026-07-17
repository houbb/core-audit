package com.github.houbb.core.audit.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;
import com.github.houbb.core.audit.infrastructure.config.AuditProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI 分析器
 * <p>通过 Spring AI 抽象调用 LLM，负责解释原因、发现关联、生成建议。
 * 当 AI 未启用时，降级为基于规则模板的本地分析。</p>
 *
 * <p>输入：Audit Event + Context + Diff + Timeline + Replay + Compliance 数据</p>
 * <p>输出：Summary + Risk + Root Cause + Suggestion</p>
 */
@Service
public class AiAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(AiAnalyzer.class);

    private final AuditProperties auditProperties;
    private final ObjectMapper objectMapper;

    public AiAnalyzer(AuditProperties auditProperties) {
        this.auditProperties = auditProperties;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * AI 分析（带降级）
     *
     * @param event 审计事件
     * @param ruleResult 规则引擎结果（可为 null）
     * @return AI 分析结果
     */
    public AiAnalysisResult analyze(AuditEvent event, RuleEngine.RuleResult ruleResult) {
        AuditProperties.Intelligence intelligence = auditProperties.getIntelligence();
        if (intelligence == null || !intelligence.isEnabled()) {
            return fallbackAnalysis(event, ruleResult);
        }

        AuditProperties.Intelligence.Ai ai = intelligence.getAi();
        if (ai == null || !ai.isEnabled()) {
            return fallbackAnalysis(event, ruleResult);
        }

        // AI 调用路径（当 Spring AI 依赖存在时）
        try {
            return callAiModel(event, ruleResult);
        } catch (Exception e) {
            log.warn("AI analysis failed, falling back to rule-based: {}", e.getMessage());
            return fallbackAnalysis(event, ruleResult);
        }
    }

    /**
     * 降级分析 — 基于模板生成分析文本
     */
    private AiAnalysisResult fallbackAnalysis(AuditEvent event, RuleEngine.RuleResult ruleResult) {
        StringBuilder summary = new StringBuilder();
        String suggestion = null;

        if (ruleResult != null) {
            summary.append("规则引擎检测到异常：").append(ruleResult.getReason()).append("。");
            summary.append("风险等级：").append(ruleResult.getLevel().name())
                   .append("（评分: ").append(ruleResult.getScore()).append("）");

            // 基于规则生成建议
            suggestion = generateSuggestion(ruleResult);
        } else {
            summary.append("操作 ").append(event.getAction()).append(" 在模块 ")
                   .append(event.getModule()).append(" 上执行，结果 ")
                   .append(event.getResult()).append("。");
            summary.append("未触发风险规则，操作正常。");
        }

        return new AiAnalysisResult(summary.toString(), suggestion);
    }

    /**
     * 基于规则生成建议
     */
    private String generateSuggestion(RuleEngine.RuleResult ruleResult) {
        String ruleName = ruleResult.getRuleName();
        if (ruleName == null) return null;

        return switch (ruleName) {
            case "midnight-delete" -> "建议启用审批流程，限制凌晨删除操作；或增加二次确认机制";
            case "bulk-delete", "bulk-delete-medium" -> "建议启用 MFA 多因素认证；增加批量删除审批流程；限制单次最大删除数量";
            case "off-hours-admin-login" -> "建议审查管理员登录记录；启用异地登录告警";
            case "sensitive-module-first-access" -> "建议对此操作人增加操作审计；确认操作授权是否合理";
            default -> null;
        };
    }

    /**
     * 调用 AI 模型（当 Spring AI 依赖可用时）
     * <p>当前为占位实现，Spring AI 依赖设为 optional。
     * 用户添加 spring-ai-openai 依赖后，此处可通过自动注入的 ChatClient 调用 LLM。</p>
     */
    private AiAnalysisResult callAiModel(AuditEvent event, RuleEngine.RuleResult ruleResult) {
        // 构建 Prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个企业审计安全分析专家。请分析以下审计事件：\n\n");
        prompt.append("操作模块: ").append(event.getModule()).append("\n");
        prompt.append("操作类型: ").append(event.getAction()).append("\n");
        prompt.append("操作人: ").append(event.getOperatorName() != null ? event.getOperatorName() : "未知").append("\n");
        prompt.append("操作目标: ").append(event.getTargetType()).append("/").append(event.getTargetId()).append("\n");
        prompt.append("操作时间: ").append(event.getOccurredAt()).append("\n");
        prompt.append("操作结果: ").append(event.getResult()).append("\n");
        if (event.getDescription() != null) {
            prompt.append("描述: ").append(event.getDescription()).append("\n");
        }
        if (ruleResult != null) {
            prompt.append("\n规则引擎发现: ").append(ruleResult.getReason())
                  .append("（等级: ").append(ruleResult.getLevel()).append("）\n");
        }
        prompt.append("\n请输出：1. 摘要 2. 风险分析 3. 改进建议");

        // TODO: 当 spring-ai-openai 依赖添加后，通过 ChatClient 调用
        // String aiResponse = chatClient.call(prompt.toString());
        log.debug("AI prompt prepared ({} chars), but AI client not configured", prompt.length());
        return fallbackAnalysis(event, ruleResult);
    }

    /**
     * AI 分析结果
     */
    public static class AiAnalysisResult {
        private final String summary;
        private final String suggestion;

        public AiAnalysisResult(String summary, String suggestion) {
            this.summary = summary;
            this.suggestion = suggestion;
        }

        public String getSummary() { return summary; }
        public String getSuggestion() { return suggestion; }
    }
}