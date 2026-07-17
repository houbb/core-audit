package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.compliance.RetentionPolicy;

import java.util.List;
import java.util.Optional;

/**
 * 保留策略仓储端口
 */
public interface RetentionPolicyRepository {

    /**
     * 保存保留策略（新增或更新）
     */
    RetentionPolicy save(RetentionPolicy policy);

    /**
     * 根据 ID 查询
     */
    Optional<RetentionPolicy> findById(String id);

    /**
     * 查询全部保留策略
     */
    List<RetentionPolicy> findAll();

    /**
     * 删除保留策略
     */
    void deleteById(String id);

    /**
     * 根据模块和操作查询策略
     */
    Optional<RetentionPolicy> findByModuleAndAction(String module, String action);

    /**
     * 查询所有启用的保留策略
     */
    List<RetentionPolicy> findAllEnabled();

    /**
     * 启用的策略总数
     */
    long countEnabled();
}
