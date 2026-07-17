package com.github.houbb.core.audit.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.github.houbb.core.audit.application.domain.compliance.AuditSignature;
import com.github.houbb.core.audit.application.port.AuditEventRepository;
import com.github.houbb.core.audit.application.port.AuditSignatureRepository;
import com.github.houbb.core.audit.infrastructure.config.AuditProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 完整性服务 — SHA-256 哈希链签名与验证
 * <p>每条审计事件在入库时生成签名，通过哈希链确保不可篡改性。</p>
 *
 * <p>哈希链结构：</p>
 * <pre>
 * Audit1 → Hash1 (previous_hash = "GENESIS")
 * Audit2 → Hash2 (previous_hash = Hash1)
 * Audit3 → Hash3 (previous_hash = Hash2)
 * </pre>
 *
 * <p>任何一条记录的修改或删除都会导致链断裂。</p>
 */
@Service
public class IntegrityService {

    private static final Logger log = LoggerFactory.getLogger(IntegrityService.class);

    /** 哈希链起点标识 */
    public static final String GENESIS = "GENESIS";

    private final AuditSignatureRepository signatureRepository;
    private final AuditEventRepository eventRepository;
    private final AuditProperties auditProperties;
    private final ObjectMapper objectMapper;

    public IntegrityService(AuditSignatureRepository signatureRepository,
                            AuditEventRepository eventRepository,
                            AuditProperties auditProperties) {
        this.signatureRepository = signatureRepository;
        this.eventRepository = eventRepository;
        this.auditProperties = auditProperties;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 为审计事件生成并保存签名
     * <p>在 AuditEventService.record() 中调用，故障隔离。</p>
     *
     * @param event 已保存的审计事件
     * @return 生成的签名
     */
    public AuditSignature sign(AuditEvent event) {
        // 1. 获取前一条签名的 hash 作为 previous_hash
        String previousHash = signatureRepository.findLatest()
                .map(AuditSignature::getHash)
                .orElse(GENESIS);

        // 2. 计算当前事件的哈希
        String hash = computeHash(event, previousHash);

        // 3. 构建并保存签名
        String algorithm = auditProperties.getCompliance().getIntegrity().getAlgorithm();
        AuditSignature signature = AuditSignature.builder()
                .id(UUID.randomUUID().toString())
                .auditId(event.getId())
                .hash(hash)
                .previousHash(previousHash)
                .algorithm(algorithm)
                .createdAt(LocalDateTime.now())
                .build();

        AuditSignature saved = signatureRepository.save(signature);
        log.debug("Signed audit {}: hash={}, previous={}", event.getId(),
                saved.getHash().substring(0, Math.min(12, saved.getHash().length())),
                saved.getPreviousHash().substring(0, Math.min(12, saved.getPreviousHash().length())));
        return saved;
    }

    /**
     * 验证单条审计事件的签名
     *
     * @param auditId 审计事件 ID
     * @return true = 签名有效
     */
    public boolean verify(String auditId) {
        Optional<AuditSignature> sigOpt = signatureRepository.findByAuditId(auditId);
        if (sigOpt.isEmpty()) {
            log.warn("No signature found for audit {}", auditId);
            return false;
        }

        Optional<AuditEvent> eventOpt = eventRepository.findById(auditId);
        if (eventOpt.isEmpty()) {
            log.warn("Audit event {} not found", auditId);
            return false;
        }

        AuditSignature stored = sigOpt.get();
        String recomputed = computeHash(eventOpt.get(), stored.getPreviousHash());
        boolean valid = stored.getHash().equals(recomputed);

        if (!valid) {
            log.warn("Hash mismatch for audit {}: stored={}, recomputed={}",
                    auditId,
                    stored.getHash().substring(0, Math.min(12, stored.getHash().length())),
                    recomputed.substring(0, Math.min(12, recomputed.length())));
        }

        return valid;
    }

    /**
     * 验证整个哈希链的完整性
     * <p>分批加载签名，逐一验证 hash 和 previous_hash 的连续性。</p>
     *
     * @param batchSize 每批加载的签名数
     * @return 链验证结果
     */
    public ChainVerificationResult verifyChain(int batchSize) {
        ChainVerificationResult result = new ChainVerificationResult();
        int offset = 0;
        String expectedPrevious = GENESIS;
        boolean firstBroken = true;

        while (true) {
            List<AuditSignature> batch = signatureRepository.findAllOrderByCreatedAt(batchSize, offset);
            if (batch.isEmpty()) break;

            result.totalCount += batch.size();

            for (AuditSignature sig : batch) {
                // 验证 previous_hash 连续性
                if (!expectedPrevious.equals(sig.getPreviousHash())) {
                    result.brokenCount++;
                    if (firstBroken) {
                        result.firstBrokenAt = sig.getAuditId();
                        firstBroken = false;
                    }
                    log.warn("Chain broken at audit {}: expected previous={}, actual={}",
                            sig.getAuditId(),
                            expectedPrevious.substring(0, Math.min(12, expectedPrevious.length())),
                            sig.getPreviousHash().substring(0, Math.min(12, sig.getPreviousHash().length())));
                }

                // 验证 hash 本身
                Optional<AuditEvent> eventOpt = eventRepository.findById(sig.getAuditId());
                if (eventOpt.isPresent()) {
                    String recomputed = computeHash(eventOpt.get(), sig.getPreviousHash());
                    if (!sig.getHash().equals(recomputed)) {
                        if (result.brokenCount == 0 || firstBroken) {
                            result.brokenCount++;
                            if (firstBroken) {
                                result.firstBrokenAt = sig.getAuditId();
                                firstBroken = false;
                            }
                        }
                        log.warn("Hash mismatch at audit {}", sig.getAuditId());
                    }
                }

                // 更新期望的下一个 previous_hash
                expectedPrevious = sig.getHash();
            }

            offset += batchSize;
            if (batch.size() < batchSize) break; // 最后一批
        }

        result.verifiedCount = result.totalCount - result.brokenCount;
        log.info("Chain verification complete: {}/{} verified, {} broken",
                result.verifiedCount, result.totalCount, result.brokenCount);
        return result;
    }

    /**
     * 计算哈希验证率（Dashboard 统计用）
     * <p>对最近 100 条签名进行抽样验证。</p>
     */
    public double calculateHashVerifyRate() {
        int sampleSize = 100;
        long total = signatureRepository.count();
        if (total == 0) return 1.0;

        List<AuditSignature> signatures = signatureRepository.findAllOrderByCreatedAt(
                sampleSize, Math.max(0, (int) total - sampleSize));
        if (signatures.isEmpty()) return 1.0;

        int verified = 0;
        for (AuditSignature sig : signatures) {
            if (verify(sig.getAuditId())) {
                verified++;
            }
        }
        return (double) verified / signatures.size();
    }

    /**
     * 计算 SHA-256 哈希
     * <p>规范化输入：按固定顺序拼接所有审计字段 + previous_hash</p>
     *
     * @param event        审计事件
     * @param previousHash 前一条签名的哈希
     * @return SHA-256 哈希值（hex 编码）
     */
    String computeHash(AuditEvent event, String previousHash) {
        StringBuilder input = new StringBuilder();
        input.append(event.getId() != null ? event.getId() : "");
        input.append("|").append(event.getModule() != null ? event.getModule().name() : "");
        input.append("|").append(event.getAction() != null ? event.getAction().name() : "");
        input.append("|").append(event.getTargetType() != null ? event.getTargetType() : "");
        input.append("|").append(event.getTargetId() != null ? event.getTargetId() : "");
        input.append("|").append(event.getOperatorId() != null ? event.getOperatorId() : "");
        input.append("|").append(event.getResult() != null ? event.getResult().name() : "");
        input.append("|").append(event.getDescription() != null ? event.getDescription() : "");
        input.append("|").append(event.getCreatedAt() != null ? event.getCreatedAt().toString() : "");
        input.append("|").append(previousHash);
        input.append("|").append(serializeMetadata(event.getMetadata()));

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return "";
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * 哈希链验证结果
     */
    public static class ChainVerificationResult {
        private int totalCount;
        private int verifiedCount;
        private int brokenCount;
        private String firstBrokenAt;

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getVerifiedCount() { return verifiedCount; }
        public void setVerifiedCount(int verifiedCount) { this.verifiedCount = verifiedCount; }
        public int getBrokenCount() { return brokenCount; }
        public void setBrokenCount(int brokenCount) { this.brokenCount = brokenCount; }
        public String getFirstBrokenAt() { return firstBrokenAt; }
        public void setFirstBrokenAt(String firstBrokenAt) { this.firstBrokenAt = firstBrokenAt; }
    }
}