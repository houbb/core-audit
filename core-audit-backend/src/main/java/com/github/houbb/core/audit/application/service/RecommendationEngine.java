package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.intelligence.AuditInsight;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;
import com.github.houbb.core.audit.application.port.AuditInsightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 建议引擎
 * <p>基于风险等级和历史 Insight 生成全局建议。
 * 全局建议是 audit_id 为空的 Insight，用于 Dashboard 展示。</p>
 *
 * <p>建议来源：</p>
 * <ul>
 *   <li>风险统计触发的系统建议</li>
 *   <li>AI 分析的改进建议</li>
 *   <li>历史 Insights 中提取的共性建议</li>
 * </ul>
 */
@Service
public class RecommendationEngine {

    private static final Logger log = LoggerFactory.getLogger(RecommendationEngine.class);

    private final AuditInsightRepository insightRepository;

    /** 全局建议缓存，避免重复生成 */
    private static final List<String> BUILTIN_RECOMMENDATIONS = List.of(
            "建议对所有 DELETE 操作启用审批流程，减少误操作风险",
            "建议启用 MFA 多因素认证，提升账户安全性",
            "建议对凌晨 00:00-06:00 的敏感操作增加二次确认",
            "建议定期审查管理员登录记录，及时发现异常登录",
            "建议配置操作频率限制，防止批量误操作",
            "建议对 CONFIG、AI 等敏感模块启用操作审计告警",
            "建议在非工作时间限制高风险操作执行权限"
    );

    public RecommendationEngine(AuditInsightRepository insightRepository) {
        this.insightRepository = insightRepository;
    }

    /**
     * 获取全局建议列表
     *
     * @param limit 建议数量上限
     * @return 全局建议列表
     */
    public List<AuditInsight> getRecommendations(int limit) {
        // 1. 先查数据库中的全局建议
        List<AuditInsight> existing = insightRepository.findGlobalRecommendations(limit);
        List<AuditInsight> recommendations = new ArrayList<>(existing);

        // 2. 如果不足，从内置建议中补充
        if (recommendations.size() < limit) {
            int needed = limit - recommendations.size();
            int builtinCount = Math.min(needed, BUILTIN_RECOMMENDATIONS.size());
            for (int i = 0; i < builtinCount; i++) {
                recommendations.add(AuditInsight.builder()
                        .auditId(null)
                        .title("安全建议 #" + (i + 1))
                        .severity(RiskLevel.MEDIUM)
                        .summary(BUILTIN_RECOMMENDATIONS.get(i))
                        .suggestion(BUILTIN_RECOMMENDATIONS.get(i))
                        .agentName("RecommendationEngine")
                        .build());
            }
        }

        return recommendations.stream().limit(limit).toList();
    }

    /**
     * 根据特定场景生成建议
     *
     * @param riskLevel 风险等级
     * @return 对应建议
     */
    public Optional<String> generateForRiskLevel(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case CRITICAL -> Optional.of("检测到严重风险事件，建议立即审查并采取补救措施");
            case HIGH -> Optional.of("检测到高风险事件，建议优先审查并及时处理");
            case MEDIUM -> Optional.of("检测到中等风险事件，建议定期审查并关注趋势");
            case LOW -> Optional.empty(); // 低风险不生成建议
        };
    }

    /**
     * 初始化内置全局建议（应用启动时调用）
     */
    public void initBuiltinRecommendations() {
        List<AuditInsight> existing = insightRepository.findGlobalRecommendations(100);
        if (!existing.isEmpty()) {
            log.debug("Global recommendations already exist ({} items), skipping init", existing.size());
            return;
        }

        log.info("Initializing built-in global recommendations");
        for (String rec : BUILTIN_RECOMMENDATIONS) {
            AuditInsight insight = AuditInsight.builder()
                    .auditId(null)
                    .title("安全建议")
                    .severity(RiskLevel.MEDIUM)
                    .summary(rec)
                    .suggestion(rec)
                    .agentName("RecommendationEngine")
                    .build();
            insightRepository.save(insight);
        }
        log.info("Initialized {} built-in recommendations", BUILTIN_RECOMMENDATIONS.size());
    }
}