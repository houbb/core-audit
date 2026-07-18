package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.application.domain.enterprise.AuditSource;
import com.github.houbb.core.audit.application.service.SourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * P9 Source Management API — 审计来源系统注册管理
 */
@RestController
@RequestMapping("/api/v1/audit/enterprise")
@Tag(name = "Enterprise — Source", description = "P9 审计来源系统管理 API")
public class SourceController {

    private final SourceService sourceService;

    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @PostMapping("/sources")
    @Operation(summary = "注册/更新来源系统")
    public ResponseEntity<AuditSource> register(@RequestBody Map<String, String> body) {
        AuditSource source = sourceService.register(
                body.get("name"),
                body.getOrDefault("type", "INTERNAL"),
                body.getOrDefault("version", "1.0"),
                body.getOrDefault("tenant", "default"),
                body.get("description"),
                body.get("endpoint")
        );
        return ResponseEntity.ok(source);
    }

    @GetMapping("/sources")
    @Operation(summary = "获取租户下所有来源系统")
    public ResponseEntity<List<AuditSource>> listSources(
            @RequestParam(defaultValue = "default") String tenant) {
        return ResponseEntity.ok(sourceService.getAllSources(tenant));
    }

    @GetMapping("/sources/active")
    @Operation(summary = "获取租户下活跃来源系统")
    public ResponseEntity<List<AuditSource>> listActiveSources(
            @RequestParam(defaultValue = "default") String tenant) {
        return ResponseEntity.ok(sourceService.getActiveSources(tenant));
    }

    @PostMapping("/sources/{id}/heartbeat")
    @Operation(summary = "来源系统心跳")
    public ResponseEntity<Void> heartbeat(@PathVariable String id) {
        sourceService.heartbeat(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/sources/{id}")
    @Operation(summary = "删除来源系统")
    public ResponseEntity<Void> deleteSource(@PathVariable String id) {
        sourceService.deleteSource(id);
        return ResponseEntity.ok().build();
    }
}