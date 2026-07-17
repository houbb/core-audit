package com.github.houbb.core.audit.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.compliance.AuditSignature;
import com.github.houbb.core.audit.application.domain.compliance.ExportTask;
import com.github.houbb.core.audit.application.domain.compliance.LegalHold;
import com.github.houbb.core.audit.application.domain.compliance.RetentionPolicy;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.domain.enums.ExportFormat;
import com.github.houbb.core.audit.application.domain.enums.ExportStatus;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditExportTaskEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditLegalHoldEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditRetentionPolicyEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSignatureEntity;

import java.time.LocalDateTime;

/**
 * Compliance 领域对象 ↔ 持久化实体 双向转换器
 * <p>与 AuditEventConverter 模式一致，使用静态方法。</p>
 */
public final class ComplianceConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ComplianceConverter() {
    }

    // ======== RetentionPolicy ========

    public static AuditRetentionPolicyEntity toRetentionPolicyEntity(RetentionPolicy domain) {
        if (domain == null) return null;
        AuditRetentionPolicyEntity entity = new AuditRetentionPolicyEntity();
        entity.setId(domain.getId());
        entity.setModule(domain.getModule() != null ? domain.getModule().name() : null);
        entity.setAction(domain.getAction() != null ? domain.getAction().name() : null);
        entity.setRetentionDays(String.valueOf(domain.getRetentionDays()));
        entity.setArchive(domain.isArchive() ? "1" : "0");
        entity.setEnabled(domain.isEnabled() ? "1" : "0");
        return entity;
    }

    public static RetentionPolicy toRetentionPolicyDomain(AuditRetentionPolicyEntity entity) {
        if (entity == null) return null;
        RetentionPolicy domain = new RetentionPolicy();
        domain.setId(entity.getId());
        domain.setModule(entity.getModule() != null ? AuditModule.valueOf(entity.getModule()) : null);
        domain.setAction(entity.getAction() != null ? AuditAction.valueOf(entity.getAction()) : null);
        domain.setRetentionDays(parseInt(entity.getRetentionDays(), 365));
        domain.setArchive("1".equals(entity.getArchive()));
        domain.setEnabled(!"0".equals(entity.getEnabled())); // default true
        return domain;
    }

    // ======== AuditSignature ========

    public static AuditSignatureEntity toSignatureEntity(AuditSignature domain) {
        if (domain == null) return null;
        AuditSignatureEntity entity = new AuditSignatureEntity();
        entity.setId(domain.getId());
        entity.setAuditId(domain.getAuditId());
        entity.setHash(domain.getHash());
        entity.setPreviousHash(domain.getPreviousHash());
        entity.setAlgorithm(domain.getAlgorithm());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt().toString() : null);
        return entity;
    }

    public static AuditSignature toSignatureDomain(AuditSignatureEntity entity) {
        if (entity == null) return null;
        AuditSignature domain = new AuditSignature();
        domain.setId(entity.getId());
        domain.setAuditId(entity.getAuditId());
        domain.setHash(entity.getHash());
        domain.setPreviousHash(entity.getPreviousHash());
        domain.setAlgorithm(entity.getAlgorithm());
        domain.setCreatedAt(entity.getCreatedAt() != null ? LocalDateTime.parse(entity.getCreatedAt()) : null);
        return domain;
    }

    // ======== LegalHold ========

    public static AuditLegalHoldEntity toLegalHoldEntity(LegalHold domain) {
        if (domain == null) return null;
        AuditLegalHoldEntity entity = new AuditLegalHoldEntity();
        entity.setId(domain.getId());
        entity.setAuditId(domain.getAuditId());
        entity.setReason(domain.getReason());
        entity.setOwner(domain.getOwner());
        entity.setExpiredAt(domain.getExpiredAt() != null ? domain.getExpiredAt().toString() : null);
        return entity;
    }

    public static LegalHold toLegalHoldDomain(AuditLegalHoldEntity entity) {
        if (entity == null) return null;
        LegalHold domain = new LegalHold();
        domain.setId(entity.getId());
        domain.setAuditId(entity.getAuditId());
        domain.setReason(entity.getReason());
        domain.setOwner(entity.getOwner());
        domain.setExpiredAt(entity.getExpiredAt() != null ? LocalDateTime.parse(entity.getExpiredAt()) : null);
        domain.setCreatedAt(entity.getCreateTime() != null ? LocalDateTime.parse(entity.getCreateTime()) : null);
        return domain;
    }

    // ======== ExportTask ========

    public static AuditExportTaskEntity toExportTaskEntity(ExportTask domain) {
        if (domain == null) return null;
        AuditExportTaskEntity entity = new AuditExportTaskEntity();
        entity.setId(domain.getId());
        entity.setQueryJson(domain.getQueryJson());
        entity.setFormat(domain.getFormat() != null ? domain.getFormat().name() : null);
        entity.setMaskEnabled(domain.isMaskEnabled() ? "1" : "0");
        entity.setIncludeDiff(domain.isIncludeDiff() ? "1" : "0");
        entity.setIncludeTimeline(domain.isIncludeTimeline() ? "1" : "0");
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
        entity.setFilePath(domain.getFilePath());
        entity.setErrorMessage(domain.getErrorMessage());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt().toString() : null);
        entity.setCompletedAt(domain.getCompletedAt() != null ? domain.getCompletedAt().toString() : null);
        return entity;
    }

    public static ExportTask toExportTaskDomain(AuditExportTaskEntity entity) {
        if (entity == null) return null;
        ExportTask domain = new ExportTask();
        domain.setId(entity.getId());
        domain.setQueryJson(entity.getQueryJson());
        domain.setFormat(parseFormat(entity.getFormat()));
        domain.setMaskEnabled(!"0".equals(entity.getMaskEnabled()));
        domain.setIncludeDiff("1".equals(entity.getIncludeDiff()));
        domain.setIncludeTimeline("1".equals(entity.getIncludeTimeline()));
        domain.setStatus(parseStatus(entity.getStatus()));
        domain.setFilePath(entity.getFilePath());
        domain.setErrorMessage(entity.getErrorMessage());
        domain.setCreatedBy(entity.getCreatedBy());
        domain.setCreatedAt(entity.getCreatedAt() != null ? LocalDateTime.parse(entity.getCreatedAt()) : null);
        domain.setCompletedAt(entity.getCompletedAt() != null ? LocalDateTime.parse(entity.getCompletedAt()) : null);
        return domain;
    }

    // ======== Helpers ========

    private static int parseInt(String s, int defaultValue) {
        if (s == null || s.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static ExportFormat parseFormat(String s) {
        if (s == null || s.isBlank()) return ExportFormat.CSV;
        try {
            return ExportFormat.valueOf(s);
        } catch (IllegalArgumentException e) {
            return ExportFormat.CSV;
        }
    }

    private static ExportStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return ExportStatus.PENDING;
        try {
            return ExportStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return ExportStatus.PENDING;
        }
    }
}