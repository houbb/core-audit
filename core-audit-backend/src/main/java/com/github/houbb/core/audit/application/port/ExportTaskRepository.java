package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.compliance.ExportTask;

import java.util.List;
import java.util.Optional;

/**
 * 导出任务仓储端口
 */
public interface ExportTaskRepository {

    /**
     * 保存导出任务（新增或更新）
     */
    ExportTask save(ExportTask task);

    /**
     * 根据 ID 查询
     */
    Optional<ExportTask> findById(String id);

    /**
     * 查询导出历史（按时间降序）
     *
     * @param limit  返回条数
     * @param offset 偏移量
     */
    List<ExportTask> findHistory(int limit, int offset);

    /**
     * 今日导出任务数
     */
    long countToday();
}