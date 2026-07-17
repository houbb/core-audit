package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.intelligence.AuditPattern;

import java.util.List;
import java.util.Optional;

/**
 * 行为模式仓储端口
 */
public interface AuditPatternRepository {

    /** 保存模式 */
    AuditPattern save(AuditPattern pattern);

    /** 根据 ID 查询 */
    Optional<AuditPattern> findById(String id);

    /** 根据类型和归属者查询 */
    Optional<AuditPattern> findByTypeAndOwner(String type, String owner);

    /** 根据类型查询所有模式 */
    List<AuditPattern> findByType(String type);

    /** 查询所有模式 */
    List<AuditPattern> findAll();
}