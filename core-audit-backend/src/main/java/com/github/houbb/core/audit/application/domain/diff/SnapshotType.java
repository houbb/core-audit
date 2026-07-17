package com.github.houbb.core.audit.application.domain.diff;

/**
 * 快照类型枚举
 * <p>区分变更前后的对象快照。</p>
 */
public enum SnapshotType {

    /** 变更前的完整对象快照 */
    BEFORE,

    /** 变更后的完整对象快照 */
    AFTER
}
