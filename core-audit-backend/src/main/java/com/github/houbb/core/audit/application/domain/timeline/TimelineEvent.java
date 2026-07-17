package com.github.houbb.core.audit.application.domain.timeline;

/**
 * 时间线与审计事件的关联 — 值对象
 * <p>表示一条 Audit Event 在某条 Timeline 中的位置。</p>
 * <p>不可变。</p>
 */
public class TimelineEvent {

    private final String timelineId;
    private final String auditId;
    private final int sequence;

    private TimelineEvent(String timelineId, String auditId, int sequence) {
        this.timelineId = timelineId;
        this.auditId = auditId;
        this.sequence = sequence;
    }

    /** 静态工厂方法 */
    public static TimelineEvent of(String timelineId, String auditId, int sequence) {
        return new TimelineEvent(timelineId, auditId, sequence);
    }

    public String getTimelineId() { return timelineId; }
    public String getAuditId() { return auditId; }
    public int getSequence() { return sequence; }
}
