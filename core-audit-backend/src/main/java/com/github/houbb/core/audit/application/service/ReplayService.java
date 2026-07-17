package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.domain.replay.ReplaySession;
import com.github.houbb.core.audit.application.domain.replay.ReplayStep;
import com.github.houbb.core.audit.application.domain.timeline.Timeline;
import com.github.houbb.core.audit.application.port.ReplayRepository;
import com.github.houbb.core.audit.application.port.TimelineRepository;
import com.github.houbb.core.audit.application.replay.ReplayStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Replay 核心服务
 * <p>编排 ReplayStrategy SPI，是 P6 的中心调度器。</p>
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li>缓存优先：先从 DB 查已缓存的 Replay，不存在时再通过 Strategy 构建</li>
 *   <li>编排所有 ReplayStrategy，找到第一个支持该 Timeline 的策略进行构建</li>
 *   <li>提供 Replay 查询及 Dashboard 统计能力</li>
 * </ul>
 *
 * <p>容错：单个 Strategy 构建失败只打 warn 日志，继续尝试下一个。</p>
 */
@Service
public class ReplayService {

    private static final Logger log = LoggerFactory.getLogger(ReplayService.class);

    private final List<ReplayStrategy> strategies;
    private final ReplayRepository replayRepository;
    private final TimelineRepository timelineRepository;

    public ReplayService(List<ReplayStrategy> strategies,
                         ReplayRepository replayRepository,
                         TimelineRepository timelineRepository) {
        this.strategies = strategies != null ? strategies : Collections.emptyList();
        this.replayRepository = replayRepository;
        this.timelineRepository = timelineRepository;
        log.info("ReplayService initialized with {} strategy(s): {}",
                this.strategies.size(),
                this.strategies.stream().map(s -> s.getClass().getSimpleName()).toList());
    }

    // ======== Build ========

    /**
     * 根据 Timeline ID 构建 Replay
     * <p>缓存优先：已存在则直接返回缓存，不存在则通过 Strategy 构建并持久化。</p>
     *
     * @param timelineId Timeline ID
     * @return ReplaySession
     */
    public Optional<ReplaySession> build(String timelineId) {
        // 1. Check cache
        Optional<ReplaySession> cached = replayRepository.findByTimelineId(timelineId);
        if (cached.isPresent()) {
            log.debug("Replay cache hit for timeline {}", timelineId);
            return cached;
        }

        // 2. Load Timeline
        Optional<Timeline> timelineOpt = timelineRepository.findById(timelineId);
        if (timelineOpt.isEmpty()) {
            log.warn("Timeline {} not found, cannot build replay", timelineId);
            return Optional.empty();
        }

        // 3. Build via strategy
        ReplaySession session = build(timelineOpt.get());
        return Optional.ofNullable(session);
    }

    /**
     * 从 Timeline 构建 ReplaySession
     * <p>遍历所有 ReplayStrategy，找到第一个支持该 Timeline 的策略进行构建。</p>
     * <p>容错：单个 Strategy 失败只打 warn 日志，继续尝试下一个。</p>
     *
     * @param timeline 时间线
     * @return ReplaySession，构建成功则缓存到 DB
     */
    public ReplaySession build(Timeline timeline) {
        for (ReplayStrategy strategy : strategies) {
            try {
                if (!strategy.supports(timeline)) {
                    continue;
                }
                ReplaySession session = strategy.build(timeline);
                if (session != null) {
                    // Cache to DB
                    replayRepository.save(session);
                    log.debug("Replay built and cached: id={}, timeline={}, steps={}",
                            session.getId(), timeline.getId(),
                            session.getSteps() != null ? session.getSteps().size() : 0);
                    return session;
                }
            } catch (Exception e) {
                log.warn("Replay strategy '{}' failed for timeline {}: {}",
                        strategy.getClass().getSimpleName(), timeline.getId(), e.getMessage());
                // 故障隔离 — 单个 Strategy 失败不阻塞
            }
        }

        log.warn("No ReplayStrategy matched timeline {}", timeline.getId());
        return null;
    }

    // ======== Query Methods ========

    /**
     * 根据 ID 获取 ReplaySession
     */
    public Optional<ReplaySession> getById(String id) {
        return replayRepository.findById(id);
    }

    /**
     * 根据 Timeline ID 获取 ReplaySession
     */
    public Optional<ReplaySession> getByTimelineId(String timelineId) {
        return replayRepository.findByTimelineId(timelineId);
    }

    /**
     * 获取 Replay 步骤列表
     */
    public List<ReplayStep> getSteps(String replayId) {
        Optional<ReplaySession> opt = replayRepository.findById(replayId);
        return opt.map(ReplaySession::getSteps).orElse(Collections.emptyList());
    }

    // ======== Dashboard ========

    public long countToday() { return replayRepository.countToday(); }
    public double avgStepsToday() { return replayRepository.avgStepsToday(); }
    public int maxStepsToday() { return replayRepository.maxStepsToday(); }
    public double avgDurationToday() { return replayRepository.avgDurationToday(); }
}