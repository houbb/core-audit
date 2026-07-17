package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.api.request.BuildReplayRequest;
import com.github.houbb.core.audit.api.response.ReplaySessionResponse;
import com.github.houbb.core.audit.api.response.ReplayStepResponse;
import com.github.houbb.core.audit.application.domain.replay.ReplaySession;
import com.github.houbb.core.audit.application.domain.replay.ReplayStep;
import com.github.houbb.core.audit.application.service.ReplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Replay API
 * <p>提供操作重放能力：构建、查看、步骤查询。</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Replay", description = "审计 Replay API")
public class ReplayController {

    private final ReplayService replayService;

    public ReplayController(ReplayService replayService) {
        this.replayService = replayService;
    }

    /**
     * 根据 ID 获取 ReplaySession（含所有步骤）
     */
    @GetMapping("/replay/{id}")
    @Operation(summary = "获取 Replay", description = "根据 ID 返回完整操作重放会话，包含所有步骤")
    public ResponseEntity<ReplaySessionResponse> getReplay(@PathVariable String id) {
        Optional<ReplaySession> opt = replayService.getById(id);
        return opt.map(session -> ResponseEntity.ok(ReplaySessionResponse.from(session)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 构建 Replay（缓存优先）
     */
    @PostMapping("/replay/build")
    @Operation(summary = "构建 Replay", description = "根据 Timeline ID 构建操作重放会话，已缓存则直接返回")
    public ResponseEntity<ReplaySessionResponse> buildReplay(@Valid @RequestBody BuildReplayRequest request) {
        Optional<ReplaySession> opt = replayService.build(request.getTimelineId());
        return opt.map(session -> ResponseEntity.ok(ReplaySessionResponse.from(session)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 获取 Replay 步骤列表
     */
    @GetMapping("/replay/{id}/steps")
    @Operation(summary = "获取 Replay 步骤", description = "返回指定 Replay 会话的所有步骤（不含会话元数据）")
    public ResponseEntity<List<ReplayStepResponse>> getReplaySteps(@PathVariable String id) {
        List<ReplayStep> steps = replayService.getSteps(id);
        if (steps.isEmpty()) {
            // 检查 replay 是否存在
            Optional<ReplaySession> opt = replayService.getById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
        }
        List<ReplayStepResponse> responses = steps.stream()
                .map(ReplayStepResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 根据 Timeline ID 获取 Replay
     */
    @GetMapping("/replay/timeline/{timelineId}")
    @Operation(summary = "Timeline Replay", description = "根据 Timeline ID 返回对应的操作重放会话")
    public ResponseEntity<ReplaySessionResponse> getReplayByTimeline(@PathVariable String timelineId) {
        Optional<ReplaySession> opt = replayService.getByTimelineId(timelineId);
        // 如果没有缓存，尝试自动构建
        if (opt.isEmpty()) {
            opt = replayService.build(timelineId);
        }
        return opt.map(session -> ResponseEntity.ok(ReplaySessionResponse.from(session)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}