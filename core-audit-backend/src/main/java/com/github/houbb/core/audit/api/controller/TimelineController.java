package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.api.response.TimelineResponse;
import com.github.houbb.core.audit.application.domain.timeline.Timeline;
import com.github.houbb.core.audit.application.service.TimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Timeline API
 * <p>提供时间线查询能力：按 ID、对象、操作人、Trace 多视角查看行为链路。</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Timeline", description = "审计 Timeline API")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    /**
     * 根据 ID 获取完整 Timeline（含所有事件）
     */
    @GetMapping("/timeline/{id}")
    @Operation(summary = "获取 Timeline", description = "根据 ID 返回完整时间线，包含所有关联的审计事件")
    public ResponseEntity<TimelineResponse> getTimeline(@PathVariable String id) {
        Optional<Timeline> opt = timelineService.getById(id);
        return opt.map(timeline -> ResponseEntity.ok(TimelineResponse.from(timeline)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 根据对象 ID 获取 Object Timeline 列表
     */
    @GetMapping("/object/{id}/timeline")
    @Operation(summary = "对象时间线", description = "返回指定对象的完整生命周期时间线")
    public ResponseEntity<List<TimelineResponse>> getObjectTimeline(@PathVariable String id) {
        List<Timeline> timelines = timelineService.getByObjectId(id);
        List<TimelineResponse> responses = timelines.stream()
                .map(TimelineResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 根据操作人 ID 获取 User Timeline 列表
     */
    @GetMapping("/operator/{id}/timeline")
    @Operation(summary = "操作人时间线", description = "返回指定操作人的所有行为时间线")
    public ResponseEntity<List<TimelineResponse>> getOperatorTimeline(@PathVariable String id) {
        List<Timeline> timelines = timelineService.getByOperatorId(id);
        List<TimelineResponse> responses = timelines.stream()
                .map(TimelineResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 根据 Trace ID 获取 Request Timeline
     */
    @GetMapping("/trace/{traceId}")
    @Operation(summary = "请求链路时间线", description = "返回指定 Trace ID 的完整调用链路")
    public ResponseEntity<List<TimelineResponse>> getTraceTimeline(@PathVariable String traceId) {
        List<Timeline> timelines = timelineService.getByTraceId(traceId);
        List<TimelineResponse> responses = timelines.stream()
                .map(TimelineResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}