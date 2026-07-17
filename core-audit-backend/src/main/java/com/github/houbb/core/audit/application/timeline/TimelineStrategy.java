package com.github.houbb.core.audit.application.timeline;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;

/**
 * 时间线策略接口（SPI 扩展点）
 * <p>不同业务对"关联"的规则不同，通过此接口实现可插拔的 Timeline 聚合策略。</p>
 *
 * <p>核心方法：</p>
 * <ul>
 *   <li>{@link #supports(AuditEvent)} — 判断该策略是否适用于此事件</li>
 *   <li>{@link #resolve(AuditEvent)} — 解析出事件归属的 Timeline 标识</li>
 *   <li>{@link #order()} — 优先级，越小越先执行</li>
 * </ul>
 *
 * <p>多归属：一个事件可能被多个策略匹配，从而归属到多条 Timeline。</p>
 *
 * <p>扩展方式：</p>
 * <p>core-ai、core-workflow 等模块可新增自己的 TimelineStrategy 实现，</p>
 * <p>注册为 Spring Bean 后自动被 TimelineService 发现。</p>
 */
public interface TimelineStrategy {

    /**
     * 判断此策略是否适用于给定事件
     *
     * @param event 审计事件
     * @return true 表示适用
     */
    boolean supports(AuditEvent event);

    /**
     * 解析事件归属的 Timeline 标识
     * <p>仅在 supports 返回 true 时调用。</p>
     *
     * @param event 审计事件
     * @return Timeline 标识（type + key）
     */
    TimelineKey resolve(AuditEvent event);

    /**
     * 策略优先级
     * <p>数值越小优先级越高，执行顺序越靠前。</p>
     * <p>建议范围：100-500。</p>
     *
     * @return 优先级
     */
    int order();

}
