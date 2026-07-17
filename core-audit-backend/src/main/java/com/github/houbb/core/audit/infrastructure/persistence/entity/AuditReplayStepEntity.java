package com.github.houbb.core.audit.infrastructure.persistence.entity;

/**
 * Replay 步骤实体
 * <p>纯 POJO，无注解，所有字段为 String，对应 audit_replay_step 表。</p>
 */
public class AuditReplayStepEntity {

    private String id;
    private String replayId;
    private String sequence;
    private String stepType;
    private String title;
    private String payloadJson;
    private String createTime;
    private String updateTime;
    private String createUser;
    private String updateUser;

    // ======== Getters & Setters ========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReplayId() { return replayId; }
    public void setReplayId(String replayId) { this.replayId = replayId; }
    public String getSequence() { return sequence; }
    public void setSequence(String sequence) { this.sequence = sequence; }
    public String getStepType() { return stepType; }
    public void setStepType(String stepType) { this.stepType = stepType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getCreateUser() { return createUser; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public String getUpdateUser() { return updateUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
}