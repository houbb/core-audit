package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.AuditEventPage;
import com.github.houbb.core.audit.application.query.AuditEventQuery;
import com.github.houbb.core.audit.application.service.AuditEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * P9 Global Search API — 跨模块统一搜索
 * <p>搜索所有 audit_event，提供企业级全局检索能力。</p>
 *
 * <p>未来可扩展为跨系统搜索（通过 Source endpoint 回调外部系统），
 * 本次 MVP 搜索 core-audit 自身数据库。</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Enterprise — Global Search", description = "P9 企业全局搜索 API")
public class GlobalSearchController {

    private final AuditEventService auditEventService;

    public GlobalSearchController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping("/search")
    @Operation(summary = "全局搜索 — 跨模块检索所有审计事件")
    public ResponseEntity<Map<String, Object>> globalSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "default") String tenant) {

        AuditEventQuery query = new AuditEventQuery();
        query.setKeyword(q);
        query.setPage(page);
        query.setSize(size);
        query.setTenant(tenant);

        AuditEventPage result = auditEventService.query(query);

        // Build enriched response with module grouping
        Map<String, Long> moduleDistribution = new LinkedHashMap<>();
        for (AuditEvent event : result.getItems()) {
            String module = event.getModule() != null ? event.getModule().name() : "UNKNOWN";
            moduleDistribution.merge(module, 1L, Long::sum);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("keyword", q);
        response.put("totalHits", result.getTotal());
        response.put("page", page);
        response.put("size", size);
        response.put("moduleDistribution", moduleDistribution);
        response.put("items", result.getItems());
        response.put("sourcesSearched", List.of("core-audit"));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/suggest")
    @Operation(summary = "搜索建议 — 快速补全")
    public ResponseEntity<List<Map<String, String>>> searchSuggest(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {

        // Quick suggest: search descriptions and target IDs
        AuditEventQuery query = new AuditEventQuery();
        query.setKeyword(q);
        query.setPage(1);
        query.setSize(limit);

        AuditEventPage result = auditEventService.query(query);

        List<Map<String, String>> suggestions = new ArrayList<>();
        for (AuditEvent event : result.getItems()) {
            Map<String, String> suggestion = new LinkedHashMap<>();
            suggestion.put("id", event.getId());
            suggestion.put("text", event.getDescription() != null ? event.getDescription() : "");
            suggestion.put("module", event.getModule() != null ? event.getModule().name() : "");
            suggestion.put("type", "audit_event");
            suggestions.add(suggestion);
        }

        return ResponseEntity.ok(suggestions);
    }
}