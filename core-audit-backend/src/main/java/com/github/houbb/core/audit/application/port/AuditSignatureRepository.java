package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.compliance.AuditSignature;

import java.util.List;
import java.util.Optional;

/**
 * 审计签名仓储端口
 */
public interface AuditSignatureRepository {

    /**
     * 保存签名
     */
    AuditSignature save(AuditSignature signature);

    /**
     * 根据审计事件 ID 查询签名
     */
    Optional<AuditSignature> findByAuditId(String auditId);

    /**
     * 获取最近一条签名（用于构建哈希链）
     *
     * @return 最近签名（按 created_at DESC）
     */
    Optional<AuditSignature> findLatest();

    /**
     * 查询全部签名（按时间升序，用于链验证）
     *
     * @param limit  返回条数
     * @param offset 偏移量
     */
    List<AuditSignature> findAllOrderByCreatedAt(int limit, int offset);

    /**
     * 签名总数
     */
    long count();

    /**
     * 删除指定审计事件的签名
     */
    void deleteByAuditId(String auditId);
}
