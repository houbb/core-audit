package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.AuditEventPage;
import com.github.houbb.core.audit.application.query.AuditEventQuery;

import java.util.Optional;

/**
 * 审计事件仓储接口（Application 层定义）
 * <p>Application 层只依赖此接口，不依赖任何具体数据库实现。</p>
 */
public interface AuditEventRepository {

    /**
     * 保存审计事件
     *
     * @param event 审计事件
     * @return 保存后的事件（包含生成的 ID 和时间）
     */
    AuditEvent save(AuditEvent event);

    /**
     * 根据 ID 查找审计事件
     *
     * @param id 事件 ID
     * @return 审计事件（可能为空）
     */
    Optional<AuditEvent> findById(String id);

    /**
     * 分页查询审计事件
     *
     * @param query 查询条件
     * @return 分页结果
     */
    AuditEventPage findAll(AuditEventQuery query);

    /**
     * 按条件计数
     *
     * @param query 查询条件
     * @return 符合条件的总条数
     */
    long countByFilter(AuditEventQuery query);

    /**
     * 今天审计总数
     *
     * @return 今日总数
     */
    long countToday();

    /**
     * 今天成功数
     *
     * @return 今日成功数
     */
    long countTodaySuccess();

    /**
     * 今天失败数
     *
     * @return 今日失败数
     */
    long countTodayFail();

    /**
     * 活跃模块数（今天有审计记录的模块数）
     *
     * @return 活跃模块数
     */
    int countActiveModulesToday();
}