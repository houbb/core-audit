package com.github.houbb.core.audit.application.domain.diff;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 变更集 — 一次业务变更的完整结构化描述
 * <p>P3 最核心的聚合对象。包含目标对象信息、所有字段级变更、操作人和时间。</p>
 */
public class ChangeSet {

    /** 目标对象类型（如 "User"、"Config"） */
    private String targetType;

    /** 目标对象 ID */
    private String targetId;

    /** 字段级变更列表 */
    private List<Change> changes;

    /** 操作人 */
    private String operator;

    /** 变更时间 */
    private LocalDateTime time;

    // ======== Constructors ========

    private ChangeSet(Builder builder) {
        this.targetType = builder.targetType;
        this.targetId = builder.targetId;
        this.changes = builder.changes != null ? Collections.unmodifiableList(builder.changes) : Collections.emptyList();
        this.operator = builder.operator;
        this.time = builder.time;
    }

    // ======== Builder ========

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String targetType;
        private String targetId;
        private List<Change> changes;
        private String operator;
        private LocalDateTime time;

        public Builder targetType(String targetType) { this.targetType = targetType; return this; }
        public Builder targetId(String targetId) { this.targetId = targetId; return this; }
        public Builder changes(List<Change> changes) { this.changes = new ArrayList<>(changes); return this; }
        public Builder addChange(Change change) {
            if (this.changes == null) this.changes = new ArrayList<>();
            this.changes.add(change);
            return this;
        }
        public Builder operator(String operator) { this.operator = operator; return this; }
        public Builder time(LocalDateTime time) { this.time = time; return this; }

        public ChangeSet build() {
            return new ChangeSet(this);
        }
    }

    // ======== Getters ========

    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public List<Change> getChanges() { return changes; }
    public String getOperator() { return operator; }
    public LocalDateTime getTime() { return time; }

    /**
     * 是否有实际变更（排除 UNCHANGED）
     */
    public boolean hasChanges() {
        return changes.stream().anyMatch(c -> c.getChangeType() != com.github.houbb.core.audit.application.domain.enums.ChangeType.UNCHANGED);
    }

    /**
     * 获取变更总数（排除 UNCHANGED）
     */
    public int changedCount() {
        return (int) changes.stream().filter(c -> c.getChangeType() != com.github.houbb.core.audit.application.domain.enums.ChangeType.UNCHANGED).count();
    }
}