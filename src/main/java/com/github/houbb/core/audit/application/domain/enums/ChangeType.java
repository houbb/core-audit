package com.github.houbb.core.audit.application.domain.enums;

/**
 * 变更类型枚举
 * <p>描述字段级别的变更类别。</p>
 *
 * <p>命名规范：与设计文档中的 ADD / REMOVE / UPDATE / UNCHANGED 一致。</p>
 */
public enum ChangeType {

    /** 新增 — before 为 null 或不存在，after 有值 */
    ADD,

    /** 删除 — before 有值，after 为 null 或不存在 */
    REMOVE,

    /** 更新 — before 和 after 都有值且不相同 */
    UPDATE,

    /** 未变更 — before 和 after 值相同 */
    UNCHANGED
}
