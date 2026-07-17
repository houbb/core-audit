package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.intelligence.AuditInsight;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;

import java.util.List;
import java.util.Optional;

/**
 * 智能洞察仓储端口
 */
public interface AuditInsightRepository {

    /** 保存洞察 */
    AuditInsight save(AuditInsight insight);

    /** 根据 ID 查询 */
    Optional<AuditInsight> findById(String id);

    /** 根据审计事件 ID 查询所有关联 Insight */
    List<AuditInsight> findByAuditId(String auditId);

    /** 查询所有洞察（按创建时间降序，分页） */
    List<AuditInsight> findAll(int page, int size);

    /** 根据严重程度查询 */
    List<AuditInsight> findBySeverity(RiskLevel severity, int page, int size);

    /** 今日 Insight 总数 */
    long countToday();

    /** 查询全局建议（audit_id 为空的 Insight） */
    List<AuditInsight> findGlobalRecommendations(int limit);
}