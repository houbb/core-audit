package com.github.houbb.core.audit.application.domain.diff;

import com.github.houbb.core.audit.application.domain.enums.ChangeType;

/**
 * 单个字段变更记录
 * <p>描述一个字段从 beforeValue 到 afterValue 的变更。</p>
 * <p>不可变值对象，通过 Builder 构造。</p>
 */
public class Change {

    /** 字段名（如 "role"、"roles[0].name"、"config.keyName"） */
    private final String fieldName;

    /** 变更类型 */
    private final ChangeType changeType;

    /** 变更前的值（字符串表示） */
    private final String beforeValue;

    /** 变更后的值（字符串表示） */
    private final String afterValue;

    // ======== Constructors ========

    private Change(Builder builder) {
        this.fieldName = builder.fieldName;
        this.changeType = builder.changeType;
        this.beforeValue = builder.beforeValue;
        this.afterValue = builder.afterValue;
    }

    // ======== Builder ========

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String fieldName;
        private ChangeType changeType;
        private String beforeValue;
        private String afterValue;

        public Builder fieldName(String fieldName) { this.fieldName = fieldName; return this; }
        public Builder changeType(ChangeType changeType) { this.changeType = changeType; return this; }
        public Builder beforeValue(String beforeValue) { this.beforeValue = beforeValue; return this; }
        public Builder afterValue(String afterValue) { this.afterValue = afterValue; return this; }

        public Change build() {
            return new Change(this);
        }
    }

    // ======== Getters ========

    public String getFieldName() { return fieldName; }
    public ChangeType getChangeType() { return changeType; }
    public String getBeforeValue() { return beforeValue; }
    public String getAfterValue() { return afterValue; }
}
