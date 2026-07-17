package com.github.houbb.core.audit.application.service;

import com.github.houbb.core.audit.application.compliance.ComplianceProvider;
import com.github.houbb.core.audit.application.domain.compliance.RetentionPolicy;
import com.github.houbb.core.audit.application.domain.enums.AuditAction;
import com.github.houbb.core.audit.application.domain.enums.AuditModule;
import com.github.houbb.core.audit.application.port.ExportTaskRepository;
import com.github.houbb.core.audit.application.port.LegalHoldRepository;
import com.github.houbb.core.audit.application.port.RetentionPolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 合规核心服务 — P7 Compliance Runtime 中心调度器
 * <p>聚合 retention、integrity、mask、export、legal hold 各项能力。</p>
 */
@Service
public class ComplianceService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceService.class);

    private final RetentionPolicyRepository retentionPolicyRepository;
    private final LegalHoldRepository legalHoldRepository;
    private final ExportTaskRepository exportTaskRepository;
    private final IntegrityService integrityService;
    private final List<ComplianceProvider> complianceProviders;

    public ComplianceService(RetentionPolicyRepository retentionPolicyRepository,
                             LegalHoldRepository legalHoldRepository,
                             ExportTaskRepository exportTaskRepository,
                             IntegrityService integrityService,
                             List<ComplianceProvider> complianceProviders) {
        this.retentionPolicyRepository = retentionPolicyRepository;
        this.legalHoldRepository = legalHoldRepository;
        this.exportTaskRepository = exportTaskRepository;
        this.integrityService = integrityService;
        this.complianceProviders = complianceProviders != null ? complianceProviders : Collections.emptyList();
        this.complianceProviders.sort((a, b) -> Integer.compare(a.order(), b.order()));
        log.info("ComplianceService initialized with {} provider(s)", this.complianceProviders.size());
    }

    // ======== Retention Policy ========

    public List<RetentionPolicy> listPolicies() {
        return retentionPolicyRepository.findAll();
    }

    public RetentionPolicy savePolicy(RetentionPolicy policy) {
        return retentionPolicyRepository.save(policy);
    }

    public void deletePolicy(String id) {
        retentionPolicyRepository.deleteById(id);
    }

    /**
     * 获取模块的有效保留策略
     * <p>优先级：数据库明确配置 > ComplianceProvider SPI > 默认策略（365 天）</p>
     */
    public RetentionPolicy getEffectivePolicy(AuditModule module, AuditAction action) {
        // 1. 检查数据库配置
        Optional<RetentionPolicy> dbPolicy = retentionPolicyRepository.findByModuleAndAction(
                module.name(), action != null ? action.name() : null);
        if (dbPolicy.isPresent()) {
            return dbPolicy.get();
        }

        // 2. 检查 SPI Provider
        for (ComplianceProvider provider : complianceProviders) {
            if (provider.supports(module)) {
                return provider.policy();
            }
        }

        // 3. 默认策略
        return RetentionPolicy.builder()
                .module(module)
                .action(action)
                .retentionDays(365)
                .archive(false)
                .enabled(true)
                .build();
    }

    // ======== Legal Hold ========

    public List<com.github.houbb.core.audit.application.domain.compliance.LegalHold> listLegalHolds() {
        return legalHoldRepository.findAll();
    }

    public com.github.houbb.core.audit.application.domain.compliance.LegalHold createLegalHold(
            com.github.houbb.core.audit.application.domain.compliance.LegalHold hold) {
        return legalHoldRepository.save(hold);
    }

    public void releaseLegalHold(String id) {
        legalHoldRepository.deleteById(id);
    }

    public boolean isUnderLegalHold(String auditId) {
        return legalHoldRepository.existsActiveByAuditId(auditId);
    }

    // ======== Integrity ========

    public Optional<com.github.houbb.core.audit.application.domain.compliance.AuditSignature> getSignature(String auditId) {
        // Delegate to IntegrityService — uses AuditSignatureRepository internally
        return null; // TODO: wire AuditSignatureRepository through IntegrityService
    }

    public boolean verifyEvent(String auditId) {
        return integrityService.verify(auditId);
    }

    public IntegrityService.ChainVerificationResult verifyChain(int batchSize) {
        return integrityService.verifyChain(batchSize);
    }

    // ======== Export ========

    public long countTodayExport() {
        return exportTaskRepository.countToday();
    }

    // ======== Overview ========

    public ComplianceOverview getOverview() {
        ComplianceOverview overview = new ComplianceOverview();
        overview.setRetentionPolicyCount(retentionPolicyRepository.countEnabled());
        overview.setLegalHoldCount(legalHoldRepository.countActive());
        overview.setTodayExportCount(exportTaskRepository.countToday());
        overview.setHashVerificationRate(integrityService.calculateHashVerifyRate());
        return overview;
    }

    public long getRetentionPolicyCount() { return retentionPolicyRepository.countEnabled(); }
    public long getLegalHoldCount() { return legalHoldRepository.countActive(); }
    public long getTodayExportCount() { return exportTaskRepository.countToday(); }
    public double getHashVerificationRate() { return integrityService.calculateHashVerifyRate(); }

    /**
     * 合规概览统计
     */
    public static class ComplianceOverview {
        private long retentionPolicyCount;
        private long legalHoldCount;
        private long todayExportCount;
        private double hashVerificationRate;

        public long getRetentionPolicyCount() { return retentionPolicyCount; }
        public void setRetentionPolicyCount(long retentionPolicyCount) { this.retentionPolicyCount = retentionPolicyCount; }
        public long getLegalHoldCount() { return legalHoldCount; }
        public void setLegalHoldCount(long legalHoldCount) { this.legalHoldCount = legalHoldCount; }
        public long getTodayExportCount() { return todayExportCount; }
        public void setTodayExportCount(long todayExportCount) { this.todayExportCount = todayExportCount; }
        public double getHashVerificationRate() { return hashVerificationRate; }
        public void setHashVerificationRate(double hashVerificationRate) { this.hashVerificationRate = hashVerificationRate; }
    }
}