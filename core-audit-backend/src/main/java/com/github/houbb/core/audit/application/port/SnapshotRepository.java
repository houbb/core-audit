package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.diff.Snapshot;
import com.github.houbb.core.audit.application.domain.diff.SnapshotType;

import java.util.List;
import java.util.Optional;

/**
 * 快照仓储接口（Application 层定义）
 * <p>用于持久化和查询变更前后的对象快照。</p>
 */
public interface SnapshotRepository {

    /**
     * 保存快照
     *
     * @param snapshot 快照对象
     * @return 保存后的快照
     */
    Snapshot save(Snapshot snapshot);

    /**
     * 根据 ID 查找快照
     */
    Optional<Snapshot> findById(String id);

    /**
     * 根据审计事件 ID 查找所有快照
     */
    List<Snapshot> findByAuditId(String auditId);

    /**
     * 根据审计事件 ID 和快照类型查找
     */
    Optional<Snapshot> findByAuditIdAndType(String auditId, SnapshotType type);
}