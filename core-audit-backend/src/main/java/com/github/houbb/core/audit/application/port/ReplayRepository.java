package com.github.houbb.core.audit.application.port;

import com.github.houbb.core.audit.application.domain.replay.ReplaySession;

import java.util.Optional;

/**
 * Replay 仓储接口（Application 层定义）
 * <p>Application 层只依赖此接口，不依赖任何具体数据库实现。</p>
 */
public interface ReplayRepository {

    /**
     * 保存 ReplaySession（含所有步骤）
     * <p>UPSERT 语义：存在则更新，不存在则插入。</p>
     *
     * @param session Replay 会话
     * @return 保存后的 Replay 会话
     */
    ReplaySession save(ReplaySession session);

    /**
     * 根据 ID 查找 ReplaySession（含步骤）
     *
     * @param id Replay ID
     * @return Replay 会话（可能为空）
     */
    Optional<ReplaySession> findById(String id);

    /**
     * 根据 Timeline ID 查找 ReplaySession
     *
     * @param timelineId Timeline ID
     * @return Replay 会话（可能为空）
     */
    Optional<ReplaySession> findByTimelineId(String timelineId);

    // ======== Dashboard 查询 ========

    /** 今日 Replay 数量 */
    long countToday();

    /** 今日平均步骤数 */
    double avgStepsToday();

    /** 今日最大步骤数 */
    int maxStepsToday();

    /** 今日平均耗时（毫秒） */
    double avgDurationToday();

}