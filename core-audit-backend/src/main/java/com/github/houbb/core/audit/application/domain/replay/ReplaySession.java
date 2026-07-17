package com.github.houbb.core.audit.application.domain.replay;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Replay 会话 — 聚合根
 * <p>P6 核心：将一条 Timeline 转换为完整操作重放序列。</p>
 * <p>Builder 模式构建，遵循 Timeline 的风格。</p>
 */
public class ReplaySession {

    /** Replay ID（UUID） */
    private String id;

    /** 关联的 Timeline ID */
    private String timelineId;

    /** 标题 */
    private String title;

    /** 总耗时（毫秒） */
    private Long duration;

    /** 步骤列表 */
    private List<ReplayStep> steps;

    /** 生成时间 */
    private LocalDateTime createdAt;

    // ======== Constructors ========

    public ReplaySession() {
        this.steps = new ArrayList<>();
    }

    private ReplaySession(Builder builder) {
        this.id = builder.id;
        this.timelineId = builder.timelineId;
        this.title = builder.title;
        this.duration = builder.duration;
        this.steps = builder.steps != null ? builder.steps : new ArrayList<>();
        this.createdAt = builder.createdAt;
    }

    // ======== Builder ========

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String timelineId;
        private String title;
        private Long duration;
        private List<ReplayStep> steps;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder timelineId(String timelineId) { this.timelineId = timelineId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder duration(Long duration) { this.duration = duration; return this; }
        public Builder steps(List<ReplayStep> steps) { this.steps = steps; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ReplaySession build() {
            return new ReplaySession(this);
        }
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTimelineId() { return timelineId; }
    public void setTimelineId(String timelineId) { this.timelineId = timelineId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public List<ReplayStep> getSteps() { return steps; }
    public void setSteps(List<ReplayStep> steps) { this.steps = steps; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}