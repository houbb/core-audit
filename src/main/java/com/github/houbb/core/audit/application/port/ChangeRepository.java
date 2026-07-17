package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.diff.Change;

import java.util.List;
import java.util.Map;

/**
 * 变更记录仓储接口（Application 层定义）
 * <p>用于持久化和查询字段级别的变更记录。</p>
 */
public interface ChangeRepository {

    /**
     * 批量保存变更记录
     *
     * @param auditId 关联的审计事件 ID
     * @param changes 变更列表
     * @return 已保存的变更列表
     */
    List<Change> saveAll(String auditId, List<Change> changes);

    /**
     * 根据审计事件 ID 查找所有变更
     */
    List<Change> findByAuditId(String auditId);

    /**
     * 按字段名和变更后值搜索
     *
     * @param fieldName  字段名（可选）
     * @param afterValue 变更后值（可选）
     * @param limit      返回条数上限
     * @param offset     偏移量
     * @return 匹配的变更列表
     */
    List<Change> search(String fieldName, String afterValue, int limit, int offset);

    /**
     * 按字段名和变更后值计数
     */
    long countBySearch(String fieldName, String afterValue);

    /**
     * 今日变更类型分布统计
     *
     * @return 变更类型 → 次数
     */
    Map<String, Long> changeTypeDistributionToday();

    /**
     * 今日变更最多的字段排名
     *
     * @param limit 返回条数
     * @return 字段排名列表
     */
    List<Map<String, Object>> topChangedFieldsToday(int limit);
}