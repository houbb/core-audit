package com.github.houbb.core.audit.application.domain.timeline;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.enums.TimelineType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间线领域对象
 * <p>P5 核心：聚合多个 Audit Event，表达一条完整的行为链路。</p>
 * <p>Builder 模式构建，遵循 AuditEvent 的风格。</p>
 */
public class Timeline {

    /** 时间线 ID（UUID） */
    private String id;

    /** 时间线类型 */
    private TimelineType type;

    /** 自动生成标题 */
    private String title;

    /** 起始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 总耗时（毫秒） */
    private Long duration;

    /** 摘要（P8 由 AI 生成，P5 留空） */
    private String summary;

    /** 关联的审计事件（查询时填充） */
    private List<AuditEvent> events;

    /** 创建时间 */
    private LocalDateTime createdAt;

    // ======== Constructors ========

    public Timeline() {
        this.events = new ArrayList<>();
    }

    private Timeline(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.title = builder.title;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.duration = builder.duration;
        this.summary = builder.summary;
        this.events = builder.events != null ? builder.events : new ArrayList<>();
        this.createdAt = builder.createdAt;
    }

    // ======== Builder ========

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private TimelineType type;
        private String title;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long duration;
        private String summary;
        private List<AuditEvent> events;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder type(TimelineType type) { this.type = type; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public Builder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
        public Builder duration(Long duration) { this.duration = duration; return this; }
        public Builder summary(String summary) { this.summary = summary; return this; }
        public Builder events(List<AuditEvent> events) { this.events = events; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Timeline build() {
            return new Timeline(this);
        }
    }

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TimelineType getType() { return type; }
    public void setType(TimelineType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<AuditEvent> getEvents() { return events; }
    public void setEvents(List<AuditEvent> events) { this.events = events; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
