package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.timeline.Timeline;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 时间线 API 响应
 * <p>遵循 AuditEventResponse 模式：from(Timeline) 静态工厂。</p>
 */
public class TimelineResponse {

    private String id;
    private String type;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String summary;
    private List<AuditEventResponse> events;
    private LocalDateTime createdAt;

    /**
     * 从领域对象构造
     */
    public static TimelineResponse from(Timeline timeline) {
        if (timeline == null) return null;
        TimelineResponse resp = new TimelineResponse();
        resp.id = timeline.getId();
        resp.type = timeline.getType() != null ? timeline.getType().name() : null;
        resp.title = timeline.getTitle();
        resp.startTime = timeline.getStartTime();
        resp.endTime = timeline.getEndTime();
        resp.duration = timeline.getDuration();
        resp.summary = timeline.getSummary();
        if (timeline.getEvents() != null) {
            resp.events = timeline.getEvents().stream()
                    .map(AuditEventResponse::from)
                    .toList();
        }
        resp.createdAt = timeline.getCreatedAt();
        return resp;
    }

    // ======== Getters ========

    public String getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Long getDuration() { return duration; }
    public String getSummary() { return summary; }
    public List<AuditEventResponse> getEvents() { return events; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
