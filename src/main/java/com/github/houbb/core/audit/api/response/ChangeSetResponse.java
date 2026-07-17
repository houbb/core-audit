package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.diff.ChangeSet;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 变更集响应
 */
public class ChangeSetResponse {

    private String targetType;
    private String targetId;
    private String operator;
    private LocalDateTime time;
    private List<ChangeResponse> changes;
    private int changedCount;
    private int totalFields;

    public static ChangeSetResponse from(ChangeSet changeSet) {
        if (changeSet == null) return null;
        ChangeSetResponse resp = new ChangeSetResponse();
        resp.targetType = changeSet.getTargetType();
        resp.targetId = changeSet.getTargetId();
        resp.operator = changeSet.getOperator();
        resp.time = changeSet.getTime();
        resp.changes = changeSet.getChanges() != null
                ? changeSet.getChanges().stream().map(ChangeResponse::from).toList()
                : Collections.emptyList();
        resp.changedCount = changeSet.changedCount();
        resp.totalFields = changeSet.getChanges().size();
        return resp;
    }

    // ======== Getters ========

    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getOperator() { return operator; }
    public LocalDateTime getTime() { return time; }
    public List<ChangeResponse> getChanges() { return changes; }
    public int getChangedCount() { return changedCount; }
    public int getTotalFields() { return totalFields; }
}
