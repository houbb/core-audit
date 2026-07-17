package com.github.houbb.core.audit.application.replay;

import com.github.houbb.core.audit.application.domain.replay.ReplaySession;
import com.github.houbb.core.audit.application.domain.timeline.Timeline;

/**
 * Replay 构建策略接口（SPI 扩展点）
 * <p>不同业务对"操作重放"的构建规则不同，通过此接口实现可插拔的 Replay 构建策略。</p>
 *
 * <p>核心方法：</p>
 * <ul>
 *   <li>{@link #supports(Timeline)} — 判断该策略是否适用于此 Timeline</li>
 *   <li>{@link #build(Timeline)} — 从 Timeline 构建 ReplaySession</li>
 * </ul>
 *
 * <p>扩展方式：</p>
 * <p>core-ai、core-workflow 等模块可新增自己的 ReplayStrategy 实现，</p>
 * <p>注册为 Spring Bean 后自动被 ReplayService 发现。</p>
 */
public interface ReplayStrategy {

    /**
     * 判断此策略是否适用于给定的 Timeline
     *
     * @param timeline 时间线
     * @return true 表示适用
     */
    boolean supports(Timeline timeline);

    /**
     * 从 Timeline 构建 ReplaySession
     * <p>仅在 supports 返回 true 时调用。</p>
     *
     * @param timeline 时间线
     * @return Replay 会话
     */
    ReplaySession build(Timeline timeline);

}