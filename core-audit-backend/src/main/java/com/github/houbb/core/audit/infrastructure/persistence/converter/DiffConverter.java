package com.github.houbb.core.audit.infrastructure.persistence.converter;

import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.diff.Snapshot;
import com.github.houbb.core.audit.application.domain.diff.SnapshotType;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditChangeEntity;
import com.github.houbb.core.audit.infrastructure.persistence.entity.AuditSnapshotEntity;

import java.time.LocalDateTime;

/**
 * Diff 相关对象双向转换器
 * <p>Snapshot ↔ AuditSnapshotEntity、Change ↔ AuditChangeEntity。</p>
 * <p>遵循 AuditEventConverter 的静态工具类模式。</p>
 */
public final class DiffConverter {

    private DiffConverter() {
    }

    // ======== Snapshot 转换 ========

    public static AuditSnapshotEntity toSnapshotEntity(Snapshot domain) {
        if (domain == null) return null;
        AuditSnapshotEntity entity = new AuditSnapshotEntity();
        entity.setId(domain.getId());
        entity.setAuditId(domain.getAuditId());
        entity.setSnapshotType(domain.getSnapshotType() != null ? domain.getSnapshotType().name() : null);
        entity.setContentJson(domain.getContentJson());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt().toString() : null);
        return entity;
    }

    public static Snapshot toSnapshotDomain(AuditSnapshotEntity entity) {
        if (entity == null) return null;
        return Snapshot.builder()
                .id(entity.getId())
                .auditId(entity.getAuditId())
                .snapshotType(entity.getSnapshotType() != null ? SnapshotType.valueOf(entity.getSnapshotType()) : null)
                .contentJson(entity.getContentJson())
                .createdAt(entity.getCreatedAt() != null ? LocalDateTime.parse(entity.getCreatedAt()) : null)
                .build();
    }

    // ======== Change 转换 ========

    public static AuditChangeEntity toChangeEntity(Change domain, String auditId) {
        if (domain == null) return null;
        AuditChangeEntity entity = new AuditChangeEntity();
        entity.setAuditId(auditId);
        entity.setFieldName(domain.getFieldName());
        entity.setChangeType(domain.getChangeType() != null ? domain.getChangeType().name() : null);
        entity.setBeforeValue(domain.getBeforeValue());
        entity.setAfterValue(domain.getAfterValue());
        return entity;
    }

    public static Change toChangeDomain(AuditChangeEntity entity) {
        if (entity == null) return null;
        return Change.builder()
                .fieldName(entity.getFieldName())
                .changeType(entity.getChangeType() != null ? ChangeType.valueOf(entity.getChangeType()) : null)
                .beforeValue(entity.getBeforeValue())
                .afterValue(entity.getAfterValue())
                .build();
    }
}