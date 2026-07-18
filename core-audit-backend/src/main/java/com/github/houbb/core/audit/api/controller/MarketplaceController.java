package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.application.domain.enterprise.AuditProvider;
import com.github.houbb.core.audit.application.service.MarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * P9 Marketplace API — 插件/Marketplace 管理
 */
@RestController
@RequestMapping("/api/v1/audit/enterprise")
@Tag(name = "Enterprise — Marketplace", description = "P9 插件/Marketplace 管理 API")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @PostMapping("/providers")
    @Operation(summary = "安装插件")
    public ResponseEntity<AuditProvider> install(@RequestBody Map<String, String> body) {
        AuditProvider provider = marketplaceService.install(
                body.get("plugin"),
                body.get("providerClass"),
                body.getOrDefault("providerType", "PROVIDER"),
                body.getOrDefault("version", "1.0"),
                body.getOrDefault("tenant", "default"),
                body.get("description"),
                body.get("author"),
                body.get("configJson")
        );
        return ResponseEntity.ok(provider);
    }

    @GetMapping("/providers")
    @Operation(summary = "获取所有已安装插件")
    public ResponseEntity<List<AuditProvider>> listProviders(
            @RequestParam(defaultValue = "default") String tenant) {
        return ResponseEntity.ok(marketplaceService.getInstalledProviders(tenant));
    }

    @GetMapping("/providers/active")
    @Operation(summary = "获取活跃插件")
    public ResponseEntity<List<AuditProvider>> listActiveProviders(
            @RequestParam(defaultValue = "default") String tenant) {
        return ResponseEntity.ok(marketplaceService.getActiveProviders(tenant));
    }

    @GetMapping("/providers/type/{providerType}")
    @Operation(summary = "按类型获取插件")
    public ResponseEntity<List<AuditProvider>> listByType(
            @PathVariable String providerType,
            @RequestParam(defaultValue = "default") String tenant) {
        return ResponseEntity.ok(marketplaceService.getProvidersByType(providerType, tenant));
    }

    @PutMapping("/providers/{id}/status")
    @Operation(summary = "更新插件状态（启用/禁用）")
    public ResponseEntity<Void> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        marketplaceService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/providers/{id}")
    @Operation(summary = "卸载插件")
    public ResponseEntity<Void> uninstall(@PathVariable String id) {
        marketplaceService.uninstall(id);
        return ResponseEntity.ok().build();
    }
}