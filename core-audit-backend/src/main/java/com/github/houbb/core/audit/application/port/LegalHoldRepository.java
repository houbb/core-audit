package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.compliance.LegalHold;

import java.util.List;
import java.util.Optional;

/**
 * 法律保留仓储端口
 */
public interface LegalHoldRepository {

    /**
     * 保存法律保留（新增或更新）
     */
    LegalHold save(LegalHold hold);

    /**
     * 根据 ID 查询
     */
    Optional<LegalHold> findById(String id);

    /**
     * 根据审计事件 ID 查询法律保留
     */
    List<LegalHold> findByAuditId(String auditId);

    /**
     * 查询全部法律保留
     */
    List<LegalHold> findAll();

    /**
     * 删除法律保留
     */
    void deleteById(String id);

    /**
     * 检查指定审计事件是否处于法律保留中
     */
    boolean existsActiveByAuditId(String auditId);

    /**
     * 当前活跃的法律保留总数
     */
    long countActive();
}