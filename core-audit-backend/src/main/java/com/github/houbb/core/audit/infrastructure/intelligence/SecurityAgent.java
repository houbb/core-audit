package com.github.houbb.core.audit.infrastructure.intelligence;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.intelligence.AuditInsight;
import com.github.houbb.core.audit.application.domain.intelligence.RiskLevel;
import com.github.houbb.core.audit.application.intelligence.AuditAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 安全分析 Agent
 * <p>专门分析安全相关操作：配置变更、密钥操作、权限变更、敏感模块访问。</p>
 */
@Component
public class SecurityAgent implements AuditAgent {

    private static final Logger log = LoggerFactory.getLogger(SecurityAgent.class);

    @Override
    public boolean supports(AuditEvent event) {
        if (event == null) return false;
        // 关注 CONFIG、AI 模块的所有操作，以及 EXPORT、IMPORT 操作
        return event.getModule() == AuditModule.CONFIG
                || event.getModule() == AuditModule.AI
                || event.getAction() == AuditAction.EXPORT
                || event.getAction() == AuditAction.IMPORT
                || event.getAction() == AuditAction.ENABLE
                || event.getAction() == AuditAction.DISABLE;
    }

    @Override
    public AuditInsight analyze(AuditEvent event) {
        log.debug("SecurityAgent analyzing: id={}, module={}, action={}",
                event.getId(), event.getModule(), event.getAction());

        StringBuilder summary = new StringBuilder();
        RiskLevel severity = RiskLevel.LOW;

        // 敏感模块操作
        if (event.getModule() == AuditModule.CONFIG || event.getModule() == AuditModule.AI) {
            summary.append("检测到敏感模块 ").append(event.getModule()).append(" 的操作。");
            severity = event.getAction() == AuditAction.DELETE ? RiskLevel.HIGH : RiskLevel.MEDIUM;
        }

        // 数据导出
        if (event.getAction() == AuditAction.EXPORT) {
            summary.append("检测到数据导出操作，可能涉及敏感数据外泄风险。");
            severity = RiskLevel.MEDIUM;
        }

        // 数据导入
        if (event.getAction() == AuditAction.IMPORT) {
            summary.append("检测到数据导入操作，需确认数据来源的安全性。");
            severity = RiskLevel.MEDIUM;
        }

        if (summary.isEmpty()) return null;

        return AuditInsight.builder()
                .auditId(event.getId())
                .title("安全分析发现")
                .severity(severity)
                .summary(summary.toString())
                .suggestion("建议：审查该操作的授权和必要性；对敏感操作启用审批流程；"
                        + "定期审计安全相关操作记录。")
                .agentName("SecurityAgent")
                .build();
    }

    @Override
    public int order() {
        return 20;
    }
}