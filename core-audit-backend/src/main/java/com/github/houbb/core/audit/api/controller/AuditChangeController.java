package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.api.response.ChangeResponse;
import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.service.AuditEventService;
import com.github.houbb.core.audit.api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 变更记录 API
 * <p>提供字段级变更查询和搜索端点。</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Changes", description = "P3 字段级变更查询 API")
public class AuditChangeController {

    private final AuditEventService auditEventService;

    public AuditChangeController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    /**
     * 获取审计事件的所有字段变更
     */
    @GetMapping("/events/{id}/changes")
    @Operation(summary = "获取审计事件的变更记录",
            description = "返回指定审计事件的所有字段级变更（before/after）")
    public ResponseEntity<Object> getChanges(@PathVariable String id) {
        return auditEventService.getById(id)
                .<ResponseEntity<Object>>map(event -> {
                    List<Change> changes = auditEventService.getChangesByAuditId(id);
                    List<ChangeResponse> response = changes.stream()
                            .map(ChangeResponse::from)
                            .toList();
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.notFound("No audit event found with id: " + id)));
    }

    /**
     * 按字段名和变更值搜索变更记录
     */
    @GetMapping("/changes/search")
    @Operation(summary = "搜索变更记录",
            description = "按字段名和/或变更后值搜索跨事件的变更记录")
    public ResponseEntity<Map<String, Object>> searchChanges(
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Change> changes = auditEventService.searchChanges(field, after, page, size);
        long total = auditEventService.countSearchChanges(field, after);

        List<ChangeResponse> items = changes.stream()
                .map(ChangeResponse::from)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("page", page);
        result.put("size", size);
        result.put("total", total);
        result.put("hasNext", (long) page * size < total);

        return ResponseEntity.ok(result);
    }
}
