package com.github.houbb.core.audit.api.response;

/**
 * 合规概览响应
 */
public class ComplianceOverviewResponse {

    private long retentionPolicyCount;
    private double hashVerificationRate;
    private long legalHoldCount;
    private long todayExportCount;

    public ComplianceOverviewResponse() {
    }

    public ComplianceOverviewResponse(long retentionPolicyCount, double hashVerificationRate,
                                       long legalHoldCount, long todayExportCount) {
        this.retentionPolicyCount = retentionPolicyCount;
        this.hashVerificationRate = hashVerificationRate;
        this.legalHoldCount = legalHoldCount;
        this.todayExportCount = todayExportCount;
    }

    public long getRetentionPolicyCount() { return retentionPolicyCount; }
    public void setRetentionPolicyCount(long retentionPolicyCount) { this.retentionPolicyCount = retentionPolicyCount; }
    public double getHashVerificationRate() { return hashVerificationRate; }
    public void setHashVerificationRate(double hashVerificationRate) { this.hashVerificationRate = hashVerificationRate; }
    public long getLegalHoldCount() { return legalHoldCount; }
    public void setLegalHoldCount(long legalHoldCount) { this.legalHoldCount = legalHoldCount; }
    public long getTodayExportCount() { return todayExportCount; }
    public void setTodayExportCount(long todayExportCount) { this.todayExportCount = todayExportCount; }
}