package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.intelligence.AuditRisk;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;

import java.util.List;
import java.util.Optional;

/**
 * 风险评分仓储端口
 */
public interface AuditRiskRepository {

    /** 保存风险评分 */
    AuditRisk save(AuditRisk risk);

    /** 根据 ID 查询 */
    Optional<AuditRisk> findById(String id);

    /** 根据审计事件 ID 查询 */
    Optional<AuditRisk> findByAuditId(String auditId);

    /** 查询所有风险评分（按评分降序，分页） */
    List<AuditRisk> findAll(int page, int size);

    /** 根据风险等级查询 */
    List<AuditRisk> findByLevel(RiskLevel level, int page, int size);

    /** 今日 Critical/High 数量 */
    long countTodayByLevel(RiskLevel level);

    /** 今日平均风险评分 */
    double avgRiskScoreToday();

    /** 今日 Insight 总数 */
    long countToday();
}