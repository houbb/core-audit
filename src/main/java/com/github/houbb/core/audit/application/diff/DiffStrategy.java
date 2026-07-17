package com.github.houbb.core.audit.application.diff;

import com.github.houbb.core.audit.application.domain.diff.ChangeSet;

/**
 * Diff 比较策略接口 — P3 核心扩展点
 * <p>不同业务对象可以拥有自己的比较算法。默认提供 Bean、JSON、Collection、Map 四种实现。</p>
 *
 * <p>实现类注册为 Spring Bean 后，DiffEngine 自动发现并注入。</p>
 *
 * @param <T> 支持的对象类型
 */
public interface DiffStrategy<T> {

    /**
     * 该策略是否支持处理给定类型
     *
     * @param type 要比较的对象类型
     * @return true 表示可以处理
     */
    boolean supports(Class<?> type);

    /**
     * 比较两个对象并生成 ChangeSet
     *
     * @param before 变更前的对象（可能为 null）
     * @param after  变更后的对象（可能为 null）
     * @return 结构化的变更集
     */
    ChangeSet compare(T before, T after);
}