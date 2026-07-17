package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.enums.TimelineType;
import com.github.houbb.core.audit.application.domain.timeline.Timeline;
import com.github.houbb.core.audit.application.domain.timeline.TimelineKey;

import java.util.List;
import java.util.Optional;

/**
 * 时间线仓储接口（Application 层定义）
 * <p>Application 层只依赖此接口，不依赖任何具体数据库实现。</p>
 */
public interface TimelineRepository {

    /**
     * 保存或更新时间线
     *
     * @param timeline 时间线
     * @return 保存后的时间线
     */
    Timeline save(Timeline timeline);

    /**
     * 根据 ID 查找时间线（含关联的 AuditEvent）
     *
     * @param id 时间线 ID
     * @return 时间线（可能为空）
     */
    Optional<Timeline> findById(String id);

    /**
     * 根据 TimelineKey 查找时间线列表
     *
     * @param key 时间线标识
     * @return 匹配的时间线列表
     */
    List<Timeline> findByKey(TimelineKey key);

    /**
     * 按类型分页查询时间线
     *
     * @param type   时间线类型
     * @param limit  返回条数
     * @param offset 偏移量
     * @return 时间线列表
     */
    List<Timeline> findByType(TimelineType type, int limit, int offset);

    /**
     * 查询最近时间线
     *
     * @param limit 返回条数
     * @return 时间线列表
     */
    List<Timeline> findRecent(int limit);

    /**
     * 向时间线追加一条事件
     *
     * @param timelineId 时间线 ID
     * @param auditId    审计事件 ID
     * @param sequence   序号
     */
    void appendEvent(String timelineId, String auditId, int sequence);

    /**
     * 获取时间线中当前最大序号
     *
     * @param timelineId 时间线 ID
     * @return 最大序号（无事件时返回 0）
     */
    int getMaxSequence(String timelineId);

    // ======== Dashboard 查询 ========

    /**
     * 今日时间线数量
     */
    long countToday();

    /**
     * 今日时间线平均事件数
     */
    double avgLengthToday();

    /**
     * 今日时间线最大事件数
     */
    int maxLengthToday();

    /**
     * 今日时间线平均耗时（毫秒）
     */
    double avgDurationToday();

}
