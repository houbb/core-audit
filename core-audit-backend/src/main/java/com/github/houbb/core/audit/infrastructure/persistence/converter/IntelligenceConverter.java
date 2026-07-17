package com.github.houbb.core.audit.infrastructure.persistence.converter;

import com.github.houbb.core.audit.application.domain.intelligence.*;
import com.github.houbb.core.audit.infrastructure.persistence.entity.*;

import java.time.LocalDateTime;

/**
 * P8 Intelligence 转换器
 * <p>Entity ↔ Domain 双向转换，静态工具类。</p>
 */
public final class IntelligenceConverter {

    private IntelligenceConverter() {
        // 禁止实例化
    }

    // ======== AuditRisk ========

    public static AuditRiskEntity toRiskEntity(AuditRisk risk) {
        if (risk == null) return null;
        AuditRiskEntity entity = new AuditRiskEntity();
        entity.setId(risk.getId());
        entity.setAuditId(risk.getAuditId());
        entity.setRiskScore(risk.getRiskScore());
        entity.setRiskLevel(risk.getRiskLevel() != null ? risk.getRiskLevel().name() : null);
        entity.setReason(risk.getReason());
        entity.setRuleName(risk.getRuleName());
        entity.setAiAnalysis(risk.getAiAnalysis());
        entity.setAnalyzedAt(risk.getAnalyzedAt() != null ? risk.getAnalyzedAt().toString() : null);
        return entity;
    }

    public static AuditRisk toRiskDomain(AuditRiskEntity entity) {
        if (entity == null) return null;
        AuditRisk risk = new AuditRisk();
        risk.setId(entity.getId());
        risk.setAuditId(entity.getAuditId());
        risk.setRiskScore(entity.getRiskScore());
        if (entity.getRiskLevel() != null && !entity.getRiskLevel().isBlank()) {
            risk.setRiskLevel(RiskLevel.valueOf(entity.getRiskLevel()));
        }
        risk.setReason(entity.getReason());
        risk.setRuleName(entity.getRuleName());
        risk.setAiAnalysis(entity.getAiAnalysis());
        if (entity.getAnalyzedAt() != null && !entity.getAnalyzedAt().isBlank()) {
            try {
                risk.setAnalyzedAt(LocalDateTime.parse(entity.getAnalyzedAt()));
            } catch (Exception ignored) {
                // ignore parse errors
            }
        }
        return risk;
    }

    // ======== AuditInsight ========

    public static AuditInsightEntity toInsightEntity(AuditInsight insight) {
        if (insight == null) return null;
        AuditInsightEntity entity = new AuditInsightEntity();
        entity.setId(insight.getId());
        entity.setAuditId(insight.getAuditId());
        entity.setTitle(insight.getTitle());
        entity.setSeverity(insight.getSeverity() != null ? insight.getSeverity().name() : null);
        entity.setSummary(insight.getSummary());
        entity.setSuggestion(insight.getSuggestion());
        entity.setEvidenceJson(insight.getEvidenceJson());
        entity.setAgentName(insight.getAgentName());
        return entity;
    }

    public static AuditInsight toInsightDomain(AuditInsightEntity entity) {
        if (entity == null) return null;
        AuditInsight insight = new AuditInsight();
        insight.setId(entity.getId());
        insight.setAuditId(entity.getAuditId());
        insight.setTitle(entity.getTitle());
        if (entity.getSeverity() != null && !entity.getSeverity().isBlank()) {
            insight.setSeverity(RiskLevel.valueOf(entity.getSeverity()));
        }
        insight.setSummary(entity.getSummary());
        insight.setSuggestion(entity.getSuggestion());
        insight.setEvidenceJson(entity.getEvidenceJson());
        insight.setAgentName(entity.getAgentName());
        return insight;
    }

    // ======== AuditPattern ========

    public static AuditPatternEntity toPatternEntity(AuditPattern pattern) {
        if (pattern == null) return null;
        AuditPatternEntity entity = new AuditPatternEntity();
        entity.setId(pattern.getId());
        entity.setType(pattern.getType());
        entity.setOwner(pattern.getOwner());
        entity.setContentJson(pattern.getContentJson());
        entity.setConfidence(pattern.getConfidence());
        entity.setSampleCount(pattern.getSampleCount());
        return entity;
    }

    public static AuditPattern toPatternDomain(AuditPatternEntity entity) {
        if (entity == null) return null;
        AuditPattern pattern = new AuditPattern();
        pattern.setId(entity.getId());
        pattern.setType(entity.getType());
        pattern.setOwner(entity.getOwner());
        pattern.setContentJson(entity.getContentJson());
        pattern.setConfidence(entity.getConfidence());
        pattern.setSampleCount(entity.getSampleCount());
        return pattern;
    }
}