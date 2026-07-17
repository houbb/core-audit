package com.github.houbb.core.audit.application.intelligence;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.intelligence.AuditInsight;

/**
 * Audit Agent SPI 接口
 * <p>插件化的 AI Agent，每个 Agent 负责一类分析场景。
 * 扩展方式：实现此接口并注册为 Spring Bean，自动被 IntelligenceService 发现。</p>
 *
 * <p>示例：</p>
 * <pre>
 * RiskAgent     — 风险评估
 * SecurityAgent — 安全分析
 * BehaviorAgent — 行为模式
 * </pre>
 */
public interface AuditAgent {

    /**
     * 判断此 Agent 是否支持分析该审计事件
     *
     * @param event 审计事件
     * @return true = 支持分析
     */
    boolean supports(AuditEvent event);

    /**
     * 执行分析并生成洞察
     *
     * @param event 审计事件
     * @return 洞察结果（可为空 = 无发现）
     */
    AuditInsight analyze(AuditEvent event);

    /**
     * Agent 优先级（数字越小优先级越高）
     *
     * @return 优先级
     */
    default int order() {
        return 100;
    }
}