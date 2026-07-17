package com.github.houbb.core.audit.application.context;

import com.github.houbb.core.audit.application.domain.context.AuditContext;

/**
 * 审计上下文提供者 SPI
 * <p>用于自动解析上下文信息。每个 Provider 负责填充 AuditContext 的一个或多个子上下文。</p>
 * <p>其他 Core 模块可通过实现此接口并注册为 @Component 来扩展上下文来源，无需修改 core-audit。</p>
 * <p>遵循与 AuditEventSubscriber 相同的 SPI 模式 — 接口定义在 application 层，实现在 infrastructure 层。</p>
 */
public interface AuditContextProvider {

    /**
     * 执行顺序（值越小越先执行）
     * <p>内置 Provider 使用：100=Request, 200=Security, 300=Client, 400=Business, 500=System</p>
     * <p>自定义 Provider 应在内置顺序之间选择合适的值。</p>
     *
     * @return 执行优先级
     */
    int order();

    /**
     * 向 AuditContext 贡献上下文数据
     * <p>由 ContextResolver 在解析过程中调用。Provider 可以读取或填充任何子上下文字段。</p>
     *
     * @param context 正在构建的 AuditContext（可变对象）
     */
    void contribute(AuditContext context);
}
