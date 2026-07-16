package com.github.houbb.core.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * core-audit 启动类
 * <p>
 * Enterprise Audit Runtime — 为整个 Core Platform 提供统一审计能力。
 * </p>
 */
@SpringBootApplication
public class CoreAuditApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreAuditApplication.class, args);
    }
}
