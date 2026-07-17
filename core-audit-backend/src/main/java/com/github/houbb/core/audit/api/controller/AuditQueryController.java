package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.api.request.AuditQueryRequest;
import com.github.houbb.core.audit.api.request.SaveQueryRequest;
import com.github.houbb.core.audit.api.response.*;
import com.github.houbb.core.audit.application.domain.query.AuditQuery;
import com.github.houbb.core.audit.application.domain.query.AuditQueryResult;
import com.github.houbb.core.audit.application.port.SavedQueryRepository.SavedQueryRecord;
import com.github.houbb.core.audit.application.port.SearchHistoryRepository.PopularQueryRecord;
import com.github.houbb.core.audit.application.port.SearchHistoryRepository.SearchHistoryRecord;
import com.github.houbb.core.audit.application.query.engine.AuditQueryEngine;
import com.github.houbb.core.audit.application.query.service.QueryHistoryService;
import com.github.houbb.core.audit.api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审计查询引擎 REST API
 * <p>P4 Search Runtime 的统一查询入口。</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Query", description = "审计查询引擎 API")
public class AuditQueryController {

    private final AuditQueryEngine auditQueryEngine;
    private final QueryHistoryService queryHistoryService;

    public AuditQueryController(AuditQueryEngine auditQueryEngine,
                                QueryHistoryService queryHistoryService) {
        this.auditQueryEngine = auditQueryEngine;
        this.queryHistoryService = queryHistoryService;
    }

    /**
     * 统一审计查询
     */
    @PostMapping("/query")
    @Operation(summary = "统一审计查询", description = "使用 AuditQuery DSL 执行审计检索，支持基础/上下文/Diff/关键字/Metadata 五类搜索范围")
    public ResponseEntity<AuditQueryResponse> query(@Valid @RequestBody AuditQueryRequest request) {
        AuditQuery query = toAuditQuery(request);
        AuditQueryResult result = auditQueryEngine.execute(query);

        // 异步记录搜索历史（简化：使用 operator 作为 userId，或默认 "anonymous"）
        String userId = request.getOperator() != null ? request.getOperator() : "anonymous";
        try {
            queryHistoryService.recordSearch(userId, query);
        } catch (Exception e) {
            // 搜索历史记录失败不影响主查询
        }

        return ResponseEntity.ok(AuditQueryResponse.from(result));
    }

    /**
     * 保存查询
     */
    @PostMapping("/query/save")
    @Operation(summary = "保存查询", description = "将当前查询条件保存为命名查询，支持团队共享")
    public ResponseEntity<SavedQueryResponse> saveQuery(@Valid @RequestBody SaveQueryRequest request) {
        AuditQuery query = toAuditQuery(request.getQuery());
        SavedQueryRecord record = queryHistoryService.saveQuery(
                request.getName(),
                request.getOwnerId(),
                query,
                request.isPublic()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(SavedQueryResponse.from(record));
    }

    /**
     * 删除已保存查询
     */
    @DeleteMapping("/query/save/{id}")
    @Operation(summary = "删除保存查询", description = "删除指定 ID 的已保存查询")
    public ResponseEntity<Object> deleteSavedQuery(@PathVariable String id) {
        boolean deleted = queryHistoryService.deleteSavedQuery(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.notFound("No saved query found with id: " + id));
    }

    /**
     * 获取已保存查询列表
     */
    @GetMapping("/query/saved")
    @Operation(summary = "获取已保存查询", description = "获取指定用户的已保存查询列表（含团队共享）")
    public ResponseEntity<List<SavedQueryResponse>> getSavedQueries(@RequestParam String userId) {
        List<SavedQueryRecord> records = queryHistoryService.getSavedQueries(userId);
        List<SavedQueryResponse> responses = records.stream()
                .map(SavedQueryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 获取最近搜索历史
     */
    @GetMapping("/query/recent")
    @Operation(summary = "获取最近搜索", description = "获取指定用户的最近搜索历史（最多 20 条）")
    public ResponseEntity<List<RecentSearchResponse>> getRecentSearches(@RequestParam String userId) {
        List<SearchHistoryRecord> records = queryHistoryService.getRecentSearches(userId);
        List<RecentSearchResponse> responses = records.stream()
                .map(RecentSearchResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 获取热门查询
     */
    @GetMapping("/query/popular")
    @Operation(summary = "获取热门查询", description = "获取搜索频次最高的查询条件（Dashboard 使用）")
    public ResponseEntity<List<PopularQueryResponse>> getPopularQueries(
            @RequestParam(defaultValue = "10") int limit) {
        List<PopularQueryRecord> records = queryHistoryService.getPopularQueries(limit);
        List<PopularQueryResponse> responses = records.stream()
                .map(PopularQueryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * AuditQueryRequest → AuditQuery DSL 转换
     */
    private AuditQuery toAuditQuery(AuditQueryRequest request) {
        return AuditQuery.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .module(request.getModule())
                .action(request.getAction())
                .result(request.getResult())
                .eventType(request.getEventType())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .operator(request.getOperator())
                .tenant(request.getTenant())
                .department(request.getDepartment())
                .role(request.getRole())
                .ip(request.getIp())
                .uri(request.getUri())
                .requestMethod(request.getRequestMethod())
                .browser(request.getBrowser())
                .os(request.getOs())
                .device(request.getDevice())
                .traceId(request.getTraceId())
                .diffField(request.getDiffField())
                .diffBefore(request.getDiffBefore())
                .diffAfter(request.getDiffAfter())
                .diffType(request.getDiffType())
                .keyword(request.getKeyword())
                .metadataKey(request.getMetadataKey())
                .metadataValue(request.getMetadataValue())
                .sortField(request.getSortField())
                .sortDirection(request.getSortDirection())
                .page(request.getPage())
                .size(request.getSize())
                .build();
    }
}