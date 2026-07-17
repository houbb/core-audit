package com.github.houbb.core.audit.api.response;

import com.github.houbb.core.audit.application.domain.diff.Change;

/**
 * 单个字段变更响应
 */
public class ChangeResponse {

    private String field;
    private String changeType;
    private String before;
    private String after;

    public static ChangeResponse from(Change change) {
        if (change == null) return null;
        ChangeResponse resp = new ChangeResponse();
        resp.field = change.getFieldName();
        resp.changeType = change.getChangeType() != null ? change.getChangeType().name() : null;
        resp.before = change.getBeforeValue();
        resp.after = change.getAfterValue();
        return resp;
    }

    // ======== Getters ========

    public String getField() { return field; }
    public String getChangeType() { return changeType; }
    public String getBefore() { return before; }
    public String getAfter() { return after; }
}
