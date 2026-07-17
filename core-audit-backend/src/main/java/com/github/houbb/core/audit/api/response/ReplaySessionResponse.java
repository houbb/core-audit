package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.replay.ReplaySession;
import com.github.houbb.core.audit.application.domain.replay.ReplayStep;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Replay 会话 API 响应
 * <p>遵循 TimelineResponse 模式：from(ReplaySession) 静态工厂。</p>
 */
public class ReplaySessionResponse {

    private String id;
    private String timelineId;
    private String title;
    private Long duration;
    private List<ReplayStepResponse> steps;
    private LocalDateTime createdAt;

    /**
     * 从领域对象构造
     */
    public static ReplaySessionResponse from(ReplaySession session) {
        if (session == null) return null;
        ReplaySessionResponse resp = new ReplaySessionResponse();
        resp.id = session.getId();
        resp.timelineId = session.getTimelineId();
        resp.title = session.getTitle();
        resp.duration = session.getDuration();
        if (session.getSteps() != null) {
            resp.steps = session.getSteps().stream()
                    .map(ReplayStepResponse::from)
                    .toList();
        } else {
            resp.steps = Collections.emptyList();
        }
        resp.createdAt = session.getCreatedAt();
        return resp;
    }

    // ======== Getters ========

    public String getId() { return id; }
    public String getTimelineId() { return timelineId; }
    public String getTitle() { return title; }
    public Long getDuration() { return duration; }
    public List<ReplayStepResponse> getSteps() { return steps; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}