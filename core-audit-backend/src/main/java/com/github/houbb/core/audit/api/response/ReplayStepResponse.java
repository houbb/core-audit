package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.replay.ReplayStep;

import java.util.Map;

/**
 * Replay 步骤 API 响应
 * <p>遵循 AuditEventResponse 模式：from(ReplayStep) 静态工厂。</p>
 */
public class ReplayStepResponse {

    private int sequence;
    private String stepType;
    private String title;
    private Map<String, Object> payload;

    /**
     * 从领域对象构造
     */
    public static ReplayStepResponse from(ReplayStep step) {
        if (step == null) return null;
        ReplayStepResponse resp = new ReplayStepResponse();
        resp.sequence = step.getSequence();
        resp.stepType = step.getStepType() != null ? step.getStepType().name() : null;
        resp.title = step.getTitle();
        resp.payload = step.getPayload();
        return resp;
    }

    // ======== Getters ========

    public int getSequence() { return sequence; }
    public String getStepType() { return stepType; }
    public String getTitle() { return title; }
    public Map<String, Object> getPayload() { return payload; }
}