package com.github.houbb.core.audit.api.exception;

import java.time.Instant;
import java.util.UUID;

/**
 * 标准错误响应体
 * <p>符合 tech 规范的 {type, title, status, detail, errorCode, traceId} 格式。</p>
 */
public class ErrorResponse {

    private String type;
    private String title;
    private int status;
    private String detail;
    private String errorCode;
    private String traceId;
    private String timestamp;

    public ErrorResponse() {
        this.traceId = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = Instant.now().toString();
    }

    // ======== Static factories ========

    public static ErrorResponse of(int status, String title, String detail, String errorCode) {
        ErrorResponse r = new ErrorResponse();
        r.type = "https://core-platform.dev/problems/" + errorCode.toLowerCase().replace('_', '-');
        r.status = status;
        r.title = title;
        r.detail = detail;
        r.errorCode = errorCode;
        return r;
    }

    public static ErrorResponse badRequest(String detail) {
        return of(400, "Bad Request", detail, "BAD_REQUEST");
    }

    public static ErrorResponse notFound(String detail) {
        return of(404, "Not Found", detail, "RESOURCE_NOT_FOUND");
    }

    public static ErrorResponse internalError(String detail) {
        return of(500, "Internal Server Error", detail, "INTERNAL_ERROR");
    }

    // ======== Getters ========

    public String getType() { return type; }
    public String getTitle() { return title; }
    public int getStatus() { return status; }
    public String getDetail() { return detail; }
    public String getErrorCode() { return errorCode; }
    public String getTraceId() { return traceId; }
    public String getTimestamp() { return timestamp; }
}