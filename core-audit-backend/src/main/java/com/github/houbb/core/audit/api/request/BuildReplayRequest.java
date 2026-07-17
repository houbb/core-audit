package com.github.houbb.core.audit.api.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 构建 Replay 请求
 * <p>遵循 CreateAuditEventRequest 模式：Jakarta Validation。</p>
 */
public class BuildReplayRequest {

    @NotBlank(message = "timelineId 不能为空")
    private String timelineId;

    public String getTimelineId() { return timelineId; }
    public void setTimelineId(String timelineId) { this.timelineId = timelineId; }
}