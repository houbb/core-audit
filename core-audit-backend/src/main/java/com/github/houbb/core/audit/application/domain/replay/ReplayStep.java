package com.github.houbb.core.audit.application.domain.replay;

import com.github.houbb.core.audit.application.domain.enums.ReplayStepType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Replay 步骤 — 值对象
 * <p>表示操作重放过程中的一个独立步骤。</p>
 * <p>不可变，通过 Builder 构造。</p>
 */
public class ReplayStep {

    /** 步骤序号 */
    private final int sequence;

    /** 步骤类型 */
    private final ReplayStepType stepType;

    /** 步骤标题 */
    private final String title;

    /** 步骤附加数据（JSON 序列化） */
    private final Map<String, Object> payload;

    // ======== Constructors ========

    private ReplayStep(Builder builder) {
        this.sequence = builder.sequence;
        this.stepType = builder.stepType;
        this.title = builder.title;
        this.payload = builder.payload != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(builder.payload))
                : Collections.emptyMap();
    }

    /**
     * 快捷工厂方法
     */
    public static ReplayStep of(int sequence, ReplayStepType stepType, String title) {
        return builder().sequence(sequence).stepType(stepType).title(title).build();
    }

    /**
     * 快捷工厂方法（带 payload）
     */
    public static ReplayStep of(int sequence, ReplayStepType stepType, String title, Map<String, Object> payload) {
        return builder().sequence(sequence).stepType(stepType).title(title).payload(payload).build();
    }

    // ======== Builder ========

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int sequence;
        private ReplayStepType stepType;
        private String title;
        private Map<String, Object> payload;

        public Builder sequence(int sequence) { this.sequence = sequence; return this; }
        public Builder stepType(ReplayStepType stepType) { this.stepType = stepType; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder payload(Map<String, Object> payload) { this.payload = payload; return this; }

        public ReplayStep build() {
            return new ReplayStep(this);
        }
    }

    // ======== Getters ========

    public int getSequence() { return sequence; }
    public ReplayStepType getStepType() { return stepType; }
    public String getTitle() { return title; }
    public Map<String, Object> getPayload() { return payload; }
}