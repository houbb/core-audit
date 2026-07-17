package com.github.houbb.core.audit.application.compliance;

import com.github.houbb.core.audit.application.domain.compliance.RetentionPolicy;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;

/**
 * 合规策略提供者（SPI 扩展点）
 * <p>不同业务模块可注册自定义的合规策略，
 * 包括保留期限、归档策略等。</p>
 *
 * <p>Spring 自动收集所有实现类（与 TimelineStrategy / ReplayStrategy 模式一致）。</p>
 */
public interface ComplianceProvider {

    /**
     * 判断此 Provider 是否支持指定模块
     *
     * @param module 审计模块
     * @return true = 支持
     */
    boolean supports(AuditModule module);

    /**
     * 返回该模块的合规策略
     *
     * @return 合规策略
     */
    RetentionPolicy policy();

    /**
     * 优先级（越小越优先）
     *
     * @return 顺序值，默认 100
     */
    default int order() {
        return 100;
    }
}
