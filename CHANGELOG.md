# CHANGELOG

## [0.10.0] - 2026-07-18

### Added — P9: Enterprise Audit Platform

P9 将 `core-audit` 从一个独立模块升级为**企业统一审计平台（Enterprise Audit Platform）**，让所有系统、所有服务、所有 Agent、所有插件共享同一套审计能力。

> 核心目标：**One Platform, One SDK, One Timeline, One Intelligence, One Governance.**

**架构升级：**

```
                    Enterprise Audit Platform

                      Gateway / SDK
                            │
          ┌─────────────────┼──────────────────┐
          │                 │                  │
    Audit Ingestion   Audit Query API   Audit Streaming
          │                 │                  │
          └─────────────────┼──────────────────┘
                            ▼
                    Audit Core Engine
                            │
  ┌─────────────┬──────────────┬──────────────┬─────────────┐
  │ Record      │ Timeline     │ Compliance   │ Intelligence│
  └─────────────┴──────────────┴──────────────┴─────────────┘
                            │
                 Unified Audit Repository
```

**P9 十二大模块：**

| 模块 | 位置 | 职责 |
|------|------|------|
| **V10 Migration** | `db/migration/V10__p9_enterprise_tables.sql` | audit_event 加 tenant 列 + audit_source / audit_provider / audit_subscription / audit_webhook_delivery 四张新表 |
| **Audit Ingestion SDK** | `application/port/AuditSdkPort.java` | Sync/Async/Batch/BatchAsync 四种写入模式 |
| **Source 注册管理** | `application/service/SourceService.java` | 来源系统注册、心跳、CRUD |
| **Multi-Tenant** | `infrastructure/tenant/TenantFilter.java` | X-Tenant-Id header → ThreadLocal → 自动填充 |
| **Plugin Marketplace** | `application/service/MarketplaceService.java` | SPI 插件安装/卸载/启用/禁用 |
| **Open Audit API** | `api/controller/EnterpriseGatewayController.java` | `/api/v1/audit/enterprise/overview` + `/health` 统一入口 |
| **Streaming Runtime** | `application/streaming/StreamingAdapter.java` + `infrastructure/streaming/LocalStreamingAdapter.java` | MQ Adapter 抽象层，MVP 本地事件总线 |
| **Webhook Runtime** | `application/service/WebhookService.java` | 异步 Webhook 分发 + HMAC 签名 + 重试 + 投递日志 |
| **Global Search** | `api/controller/GlobalSearchController.java` | `/api/v1/audit/search` 跨模块检索 + `/search/suggest` |
| **Cross-System Timeline** | `infrastructure/timeline/SourceTimelineStrategy.java` | 按 source+tenant 聚合跨系统操作链 |
| **Enterprise Dashboard** | `core-audit-web/src/pages/EnterpriseDashboardPage.vue` | 企业运营中心首页（四大统计行 + 快速入口 + Intelligence/Compliance 面板） |
| **Admin UX** | SourcesPage / ProvidersPage / SubscriptionsPage / SearchPage | 管理中心四页面：来源/插件/订阅/搜索 |

**数据库变更：**
- `ALTER TABLE audit_event ADD COLUMN tenant TEXT DEFAULT 'default'` + 2 个索引
- `audit_source` — 来源系统注册表（name, type, version, tenant, endpoint, auth_token, status）
- `audit_provider` — 插件 Marketplace 表（plugin, provider_class, provider_type, version, status）
- `audit_subscription` — Webhook 订阅表（subscriber, event_type, target, target_url, secret, retry）
- `audit_webhook_delivery` — Webhook 投递日志表（subscription_id, audit_id, response_code, duration_ms, status）

**领域模型新增：**
- `AuditSource` — 来源系统领域对象（Builder 模式）
- `AuditProvider` — SPI 插件领域对象（Builder 模式）
- `AuditSubscription` — 订阅领域对象（Builder 模式）
- `WebhookDelivery` — 投递日志领域对象（Builder 模式）

**API 新增（18 个端点）：**

| Method | Path | Description |
|--------|------|-------------|
| **Enterprise Gateway** |||
| GET | `/api/v1/audit/enterprise/overview` | 企业平台总览（聚合所有 Dashboard 指标） |
| GET | `/api/v1/audit/enterprise/health` | 平台健康检查 |
| **Source Management** |||
| POST | `/api/v1/audit/enterprise/sources` | 注册/更新来源系统 |
| GET | `/api/v1/audit/enterprise/sources` | 获取所有来源系统 |
| GET | `/api/v1/audit/enterprise/sources/active` | 获取活跃来源 |
| POST | `/api/v1/audit/enterprise/sources/{id}/heartbeat` | 来源心跳 |
| DELETE | `/api/v1/audit/enterprise/sources/{id}` | 删除来源 |
| **Marketplace** |||
| POST | `/api/v1/audit/enterprise/providers` | 安装插件 |
| GET | `/api/v1/audit/enterprise/providers` | 获取所有插件 |
| GET | `/api/v1/audit/enterprise/providers/active` | 获取活跃插件 |
| GET | `/api/v1/audit/enterprise/providers/type/{type}` | 按类型获取插件 |
| PUT | `/api/v1/audit/enterprise/providers/{id}/status` | 启用/禁用插件 |
| DELETE | `/api/v1/audit/enterprise/providers/{id}` | 卸载插件 |
| **Webhook** |||
| POST | `/api/v1/audit/enterprise/subscriptions` | 创建订阅 |
| GET | `/api/v1/audit/enterprise/subscriptions` | 获取所有订阅 |
| GET | `/api/v1/audit/enterprise/subscriptions/enabled` | 获取已启用订阅 |
| DELETE | `/api/v1/audit/enterprise/subscriptions/{id}` | 删除订阅 |
| GET | `/api/v1/audit/enterprise/subscriptions/{id}/deliveries` | 获取投递日志 |
| **Global Search** |||
| GET | `/api/v1/audit/search` | 全局搜索（含 moduleDistribution） |
| GET | `/api/v1/audit/search/suggest` | 搜索建议 |

**Dashboard 扩展：**
- `sourceCount` — 接入来源系统数
- `providerCount` — 已安装插件数
- `subscriptionCount` — Webhook 订阅数
- `webhookDeliveryCount` — 今日投递数

**SDK 增强（P9 四种写入模式）：**
```java
// ① Async（默认）— 不阻塞业务线程
audit.record(event);

// ② Sync — 需要立即获取审计 ID
AuditEvent saved = audit.recordSync(event);

// ③ Batch — 批量同步提交
List<AuditEvent> results = audit.recordBatch(events);

// ④ BatchAsync — 批量异步提交
audit.recordBatchAsync(events);
```

**Multi-Tenant 设计：**
- HTTP Header `X-Tenant-Id` → `TenantFilter`（Ordered.HIGHEST_PRECEDENCE）→ ThreadLocal
- `AuditEventService.record()` 自动从 ThreadLocal 读取 tenant，fallback "default"
- 所有查询 API 支持 `tenant` 参数过滤
- `audit_event.tenant` 列 + `context_json` 双重兼容

**Streaming MQ Adapter 抽象：**
```java
public interface StreamingAdapter {
    void publish(AuditEvent event);
    void publishBatch(List<AuditEvent> events);
    String name();
    boolean isEnabled();
}
```
- MVP: `LocalStreamingAdapter`（内存 CopyOnWriteArrayList，零依赖）
- 企业版：实现 `KafkaStreamingAdapter` 后注册为 Bean 即可替换

**Webhook Runtime 设计：**
- `@Async` 异步分发，不阻塞审计主流程
- HMAC-SHA256 签名（`X-Audit-Signature` header）
- 可配置重试次数 + 超时时间
- 每次投递记录到 `audit_webhook_delivery`（完整日志）
- Fault isolation：单个订阅失败不影响其他订阅

**Cross-System Timeline：**
- `SourceTimelineStrategy` — 按 source+tenant 聚合 Timeline（order=600）
- 同一来源系统在同一个租户下的所有事件自动串联

**前端新增（5 个页面）：**
- `EnterpriseDashboardPage.vue` — 企业运营中心首页
- `EnterpriseSourcesPage.vue` — 来源系统管理
- `EnterpriseProvidersPage.vue` — Plugin Marketplace
- `EnterpriseSubscriptionsPage.vue` — Webhook 订阅管理
- `EnterpriseSearchPage.vue` — Global Search

**前端路由新增：**
- `/enterprise` → EnterpriseDashboardPage
- `/enterprise/sources` → EnterpriseSourcesPage
- `/enterprise/providers` → EnterpriseProvidersPage
- `/enterprise/subscriptions` → EnterpriseSubscriptionsPage
- `/enterprise/search` → EnterpriseSearchPage

**Sidebar 新增：**
- "Enterprise" 导航项（building 图标），链接到 `/enterprise`

**核心设计决策：**
1. **平台化不新增业务能力** — P9 只做 SDK、Gateway、Tenant、Plugin、Streaming、Webhook，不做 Diff V2 / Replay V2
2. **SDK 四种模式** — Sync/Async/Batch/BatchAsync，覆盖所有调用场景
3. **Tenant 默认 "default"** — 向后兼容，不强制租户隔离
4. **MVP 本地事件总线** — Streaming 用 `LocalStreamingAdapter`，后续接 Kafka 不改业务代码
5. **Webhook fault isolation** — 单个 Webhook 失败不阻塞其他订阅和审计主流程
6. **Source 注册 + 心跳** — 每个接入系统有独立身份和版本追踪
7. **Plugin 类型化** — PROVIDER / TIMELINE_STRATEGY / REPLAY_STRATEGY / COMPLIANCE_PROVIDER / AUDIT_AGENT 五种 SPI 类型
8. **Global Search 单数据源** — MVP 搜索 core-audit 自身，预留 Source endpoint 回调扩展

**配置新增：**
- `core.audit.enterprise.enabled` — 企业平台总开关（默认 true）
- `core.audit.enterprise.default-tenant` — 默认租户（默认 "default"）
- `core.audit.enterprise.webhook-enabled` — Webhook 开关（默认 true）
- `core.audit.enterprise.streaming-enabled` — Streaming 开关（默认 true）
- `core.audit.enterprise.marketplace-enabled` — Marketplace 开关（默认 true）

**测试：**
149 个 JUnit 5 用例全部通过，0 失败（含 141 个现有测试 + 8 个新增 P9 测试）。

**P0～P9 完整演进：**

```
P0  Record Runtime        — 记录行为
P1  Event Runtime         — 传播事件
P2  Context Runtime       — 补全上下文
P3  Diff Runtime          — 记录变更
P4  Search Runtime        — 快速调查
P5  Timeline Runtime      — 重建行为链路
P6  Replay Runtime        — 重建操作过程
P7  Compliance Runtime    — 建立可信治理
P8  Intelligence Runtime  — 智能分析
P9  Enterprise Platform   — 企业统一审计平台
```

到 **P9**，`core-audit` 已完成从"审计日志组件"到"企业统一审计平台"的完整演进：
- **One Platform** — 所有系统共享一套审计
- **One SDK** — 四种写入模式，统一接口
- **One Timeline** — 跨系统行为串联
- **One Intelligence** — 统一 AI 分析
- **One Governance** — 统一合规治理

---

## [0.9.0] - 2026-07-18

### Added — P8: Intelligence Runtime

P8 将 `core-audit` 从"记录事实"升级为**企业智能审计平台（Audit Intelligence Platform）**，让每一条审计事件自动经过模式识别、风险评估、AI 分析和建议生成。

> 核心目标：**让 Audit 从"记录事实"升级为"理解事实、发现风险、解释原因、给出建议"。**

**架构升级：**

```
                 core-audit

            Intelligence Service
                    │
      +-------------+--------------+
      │             │              │
 Pattern Engine  Rule Engine   AI Analyzer
      │             │              │
      +-------------+--------------+
                    │
           Insight Generator
                    │
         Recommendation Engine
                    │
      +-------------+--------------+
      │             │              │
  RiskAgent   SecurityAgent   BehaviorAgent
```

**新增核心组件：**

| 组件 | 位置 | 职责 |
|------|------|------|
| `RiskLevel` | `application/domain/intelligence/` | 风险等级枚举（LOW/MEDIUM/HIGH/CRITICAL），含 `fromScore()` 映射 |
| `AuditRisk` | `application/domain/intelligence/` | 风险评分领域对象（Builder 模式，score 0-100） |
| `AuditInsight` | `application/domain/intelligence/` | 智能洞察领域对象（含 evidence_json + suggestion） |
| `AuditPattern` | `application/domain/intelligence/` | 行为模式领域对象（5 种类型 + confidence 0.0-1.0） |
| `AuditAgent` | `application/intelligence/` | Agent SPI 扩展点（`supports()` + `analyze()`，按 `order()` 排序） |
| `RuleEngine` | `application/service/` | 确定性规则引擎（5 条内置规则，短路求值） |
| `PatternEngine` | `application/service/` | 统计模式识别引擎（时间异常 / 高频操作 / 行为分布） |
| `AiAnalyzer` | `application/service/` | AI 分析器（LLM 主路径 + 模板 fallback，默认 fallback） |
| `InsightGenerator` | `application/service/` | 洞察生成器（聚合 Rule + Pattern + AI 结果） |
| `RecommendationEngine` | `application/service/` | 建议引擎（7 条内置中文建议 + 全局洞察合并） |
| `IntelligenceService` | `application/service/` | 中央编排器（7 步 pipeline，全流程故障隔离） |
| `RiskAgent` | `infrastructure/intelligence/` | DELETE/LOGIN/ENABLE/DISABLE 风险分析 Agent（order=10） |
| `SecurityAgent` | `infrastructure/intelligence/` | CONFIG/AI 模块 + EXPORT/IMPORT 安全分析 Agent（order=20） |
| `BehaviorAgent` | `infrastructure/intelligence/` | 全事件行为异常检测 Agent（凌晨操作等，order=30） |
| `IntelligenceController` | `api/controller/` | REST API（6 个端点 + 3 个内嵌 DTO） |

**数据库变更：**
- `audit_risk` — 风险评分表（audit_id FK, risk_score, risk_level, reason, rule_name, ai_analysis）
- `audit_insight` — 智能洞察表（audit_id 可空=全局洞察, title, severity, summary, suggestion, evidence_json, agent_name）
- `audit_pattern` — 行为模式表（type, owner, content_json, confidence, sample_count）
- 6 个索引覆盖 audit_id、risk_level、risk_score、severity、pattern type+owner 查询

**Rule Engine — 5 条内置规则（短路求值，首条命中即返回）：**

| 规则名 | 触发条件 | 评分 | 等级 |
|--------|----------|------|------|
| `midnight-delete` | DELETE 在 00:00-06:00 | 70 | HIGH |
| `bulk-delete` | DELETE 且 count > 100 | 95 | CRITICAL |
| `bulk-delete-medium` | DELETE 且 count > 10 | 60 | HIGH |
| `off-hours-admin-login` | Root/Admin 登录在非工作时间 | 40 | MEDIUM |
| `sensitive-module-first-access` | CONFIG/AI 模块首次访问 | 35 | MEDIUM |

**Pattern Engine — 3 种异常检测（纯统计，无需 ML）：**

| 检测器 | 触发条件 | 风险加成 |
|--------|----------|----------|
| 时间异常 | 操作时间不在典型时段（09:00-18:00）外，且样本 > 10 | +10 |
| 高频操作 | 5 分钟内 > 20 次操作 | +20 |
| 行为分布异常 | DELETE 占比 > 30%，且样本 > 20 | +15 |

**AI Analyzer — 双路径设计：**
- **AI 路径**（`ai.enabled=true`）：构建 Audit + Context + Rule 结果 Prompt → Spring AI ChatClient → LLM 分析
- **Fallback 路径**（默认）：模板化分析，从规则命中自动生成中文摘要和建议
- AI 路径依赖可选依赖 `spring-ai-openai`，不引入则自动走 fallback

**Agent SPI 设计：**

```java
public interface AuditAgent {
    boolean supports(AuditEvent event);
    AuditInsight analyze(AuditEvent event);
    default int order() { return 100; }
}
```

- 任意 Spring Bean 实现此接口即自动注册
- `IntelligenceService` 集合注入所有 Agent，按 `order()` 排序后逐个调用
- 每个 Agent 的 `analyze()` 故障隔离，单 Agent 失败不影响其他

**API 新增（6 个端点）：**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/audit/intelligence/dashboard` | Intelligence Dashboard 统计 |
| GET | `/api/v1/audit/intelligence/insights` | 分页查询洞察列表 |
| GET | `/api/v1/audit/intelligence/insights/{id}` | 查看洞察详情 |
| GET | `/api/v1/audit/intelligence/risk/{auditId}` | 查询指定 Audit 的风险评分 |
| POST | `/api/v1/audit/intelligence/analyze` | 手动触发分析（placeholder） |
| GET | `/api/v1/audit/intelligence/recommendations` | 获取全局建议列表 |

**Dashboard 扩展：**
- `todayInsightCount` — 今日洞察数
- `todayCriticalCount` — 今日 Critical 数
- `todayHighCount` — 今日 High 数
- `avgRiskScore` — 平均风险评分

**前端新增（2 个页面 + API + Types）：**

| 文件 | 说明 |
|------|------|
| `IntelligencePage.vue` | 三栏布局：左侧洞察列表 + 中间建议面板 + 右侧风险概览 + 顶部 4 个统计卡片 |
| `InsightDetailPage.vue` | 洞察详情：标题+严重度、摘要卡片、风险评分卡片、建议卡片、Evidence 网格 |
| `api/intelligence.ts` | 5 个 API 调用函数 |
| `types/audit.ts` | AuditRisk / AuditInsight / IntelligenceDashboard 类型定义 |

**前端路由新增：**
- `/intelligence` → IntelligencePage（三栏布局）
- `/intelligence/insights/:id` → InsightDetailPage（面包屑导航）

**核心设计决策：**

1. **Rule First, AI Enhanced** — 明确规则负责命中（DELETE/LOGIN/PERMISSION/CONFIG），AI 负责解释和建议，不反过来
2. **Fault isolation throughout** — Pattern/Rule/AI/Agent 每一步失败只记日志，不阻塞审计记录主流程
3. **Evidence Driven** — 所有 AI 结论必须引用 Evidence（Timeline/Replay/Diff/Audit），不凭空生成
4. **Explainable AI** — 每个 Risk 必须解释"为什么"（凌晨 + 批量删除 + 历史首次发生 + 涉及生产租户）
5. **Agent 化** — 不做一个大 Prompt，而是多个独立 Agent 插件，未来扩展只需新增实现类
6. **Composite risk scoring** — 最终风险分 = max(rule score, rule score + pattern anomaly bonus)，上限 100
7. **AI disabled by default** — AI 路径默认关闭，不引入 `spring-ai-openai` 依赖，企业按需开启
8. **Global insights** — `audit_id IS NULL` 的洞察为全局建议，由 `RecommendationEngine` 管理
9. **与 RCA 深度融合** — P8 开始，core-audit 成为 RCA 最重要的数据底座，可直接消费 P0-P7 全部数据

**P8 不做（留到后续 Phase）：**

| 能力 | Phase | 原因 |
|------|-------|------|
| 企业知识图谱 | P9 | 跨系统关联分析 |
| 自动修复（Auto Remediation） | P9 | 涉及执行权限和审批 |
| AI 自主审批 | P9 | 高风险能力，不建议默认开启 |
| 多企业模型训练 | P9 | 企业级平台能力 |
| 联邦学习 | P9 | 超大规模部署场景 |

**P0～P8 能力演进：**

```
P0  Record Runtime        — 记录行为
P1  Event Runtime         — 传播事件
P2  Context Runtime       — 补全上下文
P3  Diff Runtime          — 记录变更
P4  Search Runtime        — 快速调查
P5  Timeline Runtime      — 重建行为链路
P6  Replay Runtime        — 重建操作过程
P7  Compliance Runtime    — 建立可信治理
P8  Intelligence Runtime  — 智能分析与 RCA 基础平台
```

到 **P8**，`core-audit` 已完成从"数据记录"到"智能分析"的质变：
- **理解（Understand）** — Pattern Engine 识别行为模式
- **推理（Reason）** — Rule Engine + AI Analyzer 分析风险
- **预测（Predict）** — 行为基线对比，异常检测
- **建议（Recommend）** — 7 条内置建议 + AI 动态生成

**配置新增：**
- `core.audit.intelligence.enabled` — Intelligence 总开关（默认 true）
- `core.audit.intelligence.rule.enabled` — Rule Engine 开关（默认 true）
- `core.audit.intelligence.ai.enabled` — AI 分析开关（默认 false）
- `core.audit.intelligence.ai.provider` — AI 提供商（默认 openai）
- `core.audit.intelligence.ai.model` — AI 模型（默认 gpt-4o-mini）

**测试：**
全量 JUnit 5 用例通过，0 失败（含现有测试 + 新增 P8 编译兼容）。

---

## [0.8.0] - 2026-07-17

### Added — P7: Compliance Runtime

P7 将 `core-audit` 从"技术审计平台"升级为**企业级合规平台（Enterprise Compliance Platform）**，让审计数据满足企业治理、法律法规、内控要求。

> 核心目标：**让 Audit 从"可以查看"升级为"可以作为企业合规和法律证据使用"。**

**架构升级：**

```
                 core-audit

            Compliance Service
                    │
     +--------------+--------------+
     │              │              │
Retention      Integrity      Mask Engine
     │              │              │
     +--------------+--------------+
                    │
          Compliance Repository
                    │
             Audit Repository
```

**新增六大核心组件：**

| 组件 | 位置 | 职责 |
|------|------|------|
| `RetentionPolicy` | `application/domain/compliance/` | 保留策略领域对象（Builder 模式） |
| `AuditSignature` | `application/domain/compliance/` | SHA-256 哈希链签名 |
| `LegalHold` | `application/domain/compliance/` | 法律保留（覆盖 Retention 策略） |
| `ExportTask` | `application/domain/compliance/` | 异步导出任务（CSV / Excel） |
| `ComplianceProvider` | `application/compliance/` | 合规策略 SPI 扩展点 |
| `MaskStrategy` | `application/compliance/` | 脱敏策略 SPI 扩展点 |
| `@AuditMask` | `application/compliance/` | 敏感字段脱敏注解（与 @AuditIgnore 同模式） |
| `IntegrityService` | `application/service/` | SHA-256 哈希链签名 + 验证引擎 |
| `MaskService` | `application/service/` | 展示层自动脱敏（@AuditMask + 字段名检测） |
| `ExportService` | `application/service/` | @Async 异步导出（CSV + Excel） |
| `ComplianceService` | `application/service/` | 合规中心调度器（聚合所有合规能力） |
| `DefaultMaskStrategy` | `infrastructure/compliance/` | 内置 6 种敏感类型脱敏实现 |
| `ComplianceScheduler` | `infrastructure/scheduler/` | `@Scheduled` 保留策略执行 Job（每日 02:00） |
| `ComplianceController` | `api/controller/` | REST API（14 个端点） |

**数据库变更：**
- `audit_retention_policy` — 保留策略表（module + action → retention_days + archive）
- `audit_signature` — 完整性签名表（audit_id UNIQUE, hash, previous_hash, algorithm）
- `audit_legal_hold` — 法律保留表（audit_id, reason, owner, expired_at）
- `audit_export_task` — 导出任务表（query_json, format, status, file_path）

**新增枚举：**
- `SensitiveType` — 6 种敏感数据类型（PHONE / EMAIL / TOKEN / PASSWORD / SECRET / API_KEY）
- `ExportFormat` — 3 种导出格式（CSV / EXCEL / PDF）
- `ExportStatus` — 4 种导出状态（PENDING / PROCESSING / COMPLETED / FAILED）

**API 新增（14 个端点）：**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/audit/compliance/overview` | 合规概览 Dashboard 统计 |
| GET | `/api/v1/audit/compliance/policies` | 列出所有保留策略 |
| POST | `/api/v1/audit/compliance/policies` | 创建/更新保留策略 |
| DELETE | `/api/v1/audit/compliance/policies/{id}` | 删除保留策略 |
| GET | `/api/v1/audit/compliance/hash/{auditId}` | 获取签名 + 验证状态 |
| POST | `/api/v1/audit/compliance/verify` | 验证哈希链完整性 |
| GET | `/api/v1/audit/compliance/legal-holds` | 列出所有法律保留 |
| POST | `/api/v1/audit/compliance/legal-hold` | 创建法律保留 |
| DELETE | `/api/v1/audit/compliance/legal-hold/{id}` | 释放法律保留 |
| POST | `/api/v1/audit/compliance/export` | 提交异步导出任务 |
| GET | `/api/v1/audit/compliance/export/{taskId}` | 查询导出任务状态 |
| GET | `/api/v1/audit/compliance/export/{taskId}/download` | 下载导出文件 |
| GET | `/api/v1/audit/compliance/export/history` | 导出历史列表 |

**Dashboard 扩展：**
- `hashVerifyRate` — 哈希验证率（最近 100 条抽样）
- `legalHoldCount` — 活跃法律保留数
- `retentionPolicyCount` — 启用的保留策略数
- `todayExportCount` — 今日导出任务数

**Integrity Engine — SHA-256 哈希链设计：**

```
Audit1 → Hash1 (previous_hash = "GENESIS")
Audit2 → Hash2 (previous_hash = Hash1)
Audit3 → Hash3 (previous_hash = Hash2)
```

- 每条 Audit 入库时自动签名（`IntegrityService.sign()` 在 `record()` 流程中执行，故障隔离）
- 哈希链任意一点断裂（修改/删除）→ `verifyChain()` 立即检测
- 规范化输入：id + module + action + targetType + targetId + operatorId + result + description + createdAt + previousHash + metadata

**Mask Engine — @AuditMask 注解设计：**

```java
@AuditMask(type = SensitiveType.PHONE)
private String phone; // 13812345678 → 138****5678
```

- 脱敏发生在展示层（`AuditEventResponse.from()`），原始数据在 DB 中不变
- 优先级：@AuditMask 注解 > MaskStrategy.detectByFieldName() > 不脱敏
- 内置 6 种 SensitiveType 脱敏逻辑

**核心设计决策：**
1. **Sign at record time** — 签名在 Audit 入库后立即生成，不延迟
2. **Hash Chain** — `previous_hash` 链接前一条 Audit 的 hash，形成不可断裂的链
3. **Mask at presentation layer** — `AuditEventResponse.from()` 静态工厂中自动应用脱敏
4. **Export async** — POST 立即返回 PENDING，`@Async` 后台执行，GET 轮询
5. **Legal Hold > Retention** — 法律保留期间审计记录不可被 Retention Job 删除
6. **ComplianceProvider SPI** — 与 TimelineStrategy / ReplayStrategy 同模式，按 `order()` 排序
7. **Fault isolation** — 签名失败/脱敏失败只记日志，不阻塞审计记录主流程
8. **ComplianceScheduler** — `@Scheduled(cron = "0 0 2 * * *")` 每日凌晨 2 点执行保留清理

**P7 不做（留到后续 Phase）：**

| 能力 | Phase | 原因 |
|------|-------|------|
| AI 自动合规检查 | P8 | AI 分析属于智能层 |
| 多区域法规模板 | P9 | 企业全球部署 |
| WORM 存储 | P9 | 与对象存储能力结合 |
| 区块链存证 | P9 | 极少数行业需要，不作为 Core 默认 |
| PDF 导出 | P7.1 | 本次只做 CSV + Excel |

**P0～P7 能力演进：**

```
P0  Record Runtime    — 记录行为
P1  Event Runtime     — 传播事件
P2  Context Runtime   — 补全上下文
P3  Diff Runtime      — 记录变更
P4  Search Runtime    — 快速调查
P5  Timeline Runtime  — 重建行为链路
P6  Replay Runtime    — 重建操作过程
P7  Compliance Runtime — 建立可信、可审计、可合规的企业级审计体系
```

到 **P7**，`core-audit` 已具备企业级审计平台最核心的三项能力：
- **可信（Trustworthy）**：哈希链、保留策略、法律保留、脱敏
- **可调查（Investigable）**：Search、Timeline、Replay
- **可证明（Provable）**：导出、签名、完整性验证、合规报告

**配置新增：**
- `core.audit.compliance.enabled` — Compliance 总开关（默认 true）
- `core.audit.compliance.integrity.algorithm` — 哈希算法（默认 SHA-256）
- `core.audit.compliance.mask.enabled` — 脱敏开关（默认 true）

**依赖新增：**
- Apache POI 5.2.5（Excel 导出）

**测试：**
133 个 JUnit 5 用例全部通过，0 失败（含现有 133 个 + 新增 P7 编译兼容）。

---

## [0.7.0] - 2026-07-17

### Added — P6: Replay Runtime

P6 将 `core-audit` 从"知道发生了什么"升级为**完整操作重放（Operation Replay）**，把 Timeline 转换为可逐步回放的操作过程。

> 核心目标：**重建完整操作过程，支持回放、调查、培训和故障分析。**

**架构升级：**

```
Audit Event → Timeline → ReplayStrategy (SPI) → ReplayService → ReplaySession → Player
                    │
            DefaultReplayStrategy (catch-all, with LOGIN/REQUEST/DATABASE/AUDIT/EVENT/FINISH steps)
```

**新增核心组件：**

| 组件 | 位置 | 职责 |
|------|------|------|
| `ReplaySession` | `application/domain/replay/` | Replay 聚合根（Builder 模式，含步骤列表） |
| `ReplayStep` | `application/domain/replay/` | 操作步骤值对象（sequence + stepType + title + payload） |
| `ReplayStepType` | `application/domain/enums/` | LOGIN / OPEN_PAGE / CLICK_BUTTON / INPUT / REQUEST / DATABASE / AUDIT / EVENT / FINISH |
| `ReplayStrategy` | `application/replay/` | 可插拔 Replay 构建策略（SPI 扩展点） |
| `DefaultReplayStrategy` | `infrastructure/replay/` | 默认通用策略，从 AuditEvent 序列生成 ReplayStep 序列 |
| `ReplayService` | `application/service/` | 核心服务（缓存优先 + Strategy 编排 + 容错） |
| `ReplayRepository` | `application/port/` | Replay 仓储端口 |
| `JdbcReplayRepository` | `infrastructure/persistence/repository/` | JDBC 实现（SQLite/MySQL 双方言，UPSERT） |
| `ReplayController` | `api/controller/` | REST API（4 个端点） |

**数据库变更：**
- `audit_replay` — Replay 主表（timeline_id UNIQUE，一条 Timeline 一条 Replay）
- `audit_replay_step` — Replay 步骤表（按 sequence 排序）

**API 新增：**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/audit/replay/{id}` | 获取完整 ReplaySession（含所有步骤） |
| POST | `/api/v1/audit/replay/build` | 构建 Replay（缓存优先） |
| GET | `/api/v1/audit/replay/{id}/steps` | 获取步骤列表 |
| GET | `/api/v1/audit/replay/timeline/{timelineId}` | 根据 Timeline 获取 Replay |

**Dashboard 扩展：**
- `todayReplayCount` — 今日 Replay 数量
- `avgReplaySteps` — 平均步骤数
- `maxReplaySteps` — 最大步骤数
- `avgReplayDuration` — 平均耗时

**核心设计决策：**
1. **缓存优先** — 第一次 build 写入 DB，后续查询直接读取，不重复构建
2. **First-match-wins** — 与 Timeline 的多归属不同，Replay 采用首个匹配策略（通过 DefaultReplayStrategy 兜底）
3. **Replay 不执行** — 只重建过程，不重新调用接口或修改数据
4. **结构化步骤** — 9 种固定 ReplayStepType，确保前端播放器的一致性
5. **插件化构建** — ReplayStrategy SPI 支持不同业务注册自定义构建策略

---

## [0.6.0] - 2026-07-17

### Added — P5: Timeline Runtime

P5 将 `core-audit` 从"离散日志记录"升级为**统一行为时间线（Behavior Timeline）**，把 Audit Event 串联成完整的故事。

> 核心目标：**围绕一个对象、一名用户、一个请求、一场事故，重建完整的行为轨迹。**

**架构升级：**

```
Audit Event → TimelineStrategy (SPI) → Correlation Engine → Timeline Builder → Story
                    │
        Trace | Session | Resource | Operator | Workflow
```

**新增核心组件：**

| 组件 | 位置 | 职责 |
|------|------|------|
| `Timeline` | `application/domain/timeline/` | 时间线领域对象（Builder 模式） |
| `TimelineKey` | `application/domain/timeline/` | 时间线标识值对象（type + key） |
| `TimelineType` | `application/domain/enums/` | USER / OBJECT / REQUEST / SESSION / INCIDENT / WORKFLOW |
| `TimelineStrategy` | `application/timeline/` | 可插拔时间线聚合策略（SPI 扩展点） |
| `TraceTimelineStrategy` | `infrastructure/timeline/` | 按 Trace ID 聚合（order=100） |
| `SessionTimelineStrategy` | `infrastructure/timeline/` | 按 Session ID 聚合（order=200） |
| `ResourceTimelineStrategy` | `infrastructure/timeline/` | 按 targetType#targetId 聚合（order=300） |
| `OperatorTimelineStrategy` | `infrastructure/timeline/` | 按操作人 ID 聚合（order=400） |
| `WorkflowTimelineStrategy` | `infrastructure/timeline/` | 按 Workflow ID 聚合（order=500） |
| `TimelineService` | `application/service/` | 核心服务（Correlation Engine + Story Builder） |
| `TimelineRepository` | `application/port/` | 时间线仓储端口 |
| `JdbcTimelineRepository` | `infrastructure/persistence/repository/` | JDBC 实现（SQLite/MySQL 双方言） |
| `TimelineController` | `api/controller/` | REST API（4 个端点） |
| `TimelinePage.vue` | `core-audit-web/src/pages/` | 三栏布局时间线页面 |
| `TimelineListPage.vue` | `core-audit-web/src/pages/` | 对象/操作人/Trace 时间线列表 |
| `TimelineNode.vue` | `core-audit-web/src/components/` | 时间轴节点组件 |

**数据库变更：**
- `audit_timeline` — 时间线主表
- `audit_timeline_event` — Timeline 与 Audit Event 多对多关联

**API 新增：**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/audit/timeline/{id}` | 获取完整 Timeline（含所有事件） |
| GET | `/api/v1/audit/object/{id}/timeline` | 对象生命周期时间线 |
| GET | `/api/v1/audit/operator/{id}/timeline` | 操作人行为时间线 |
| GET | `/api/v1/audit/trace/{traceId}` | 请求链路时间线 |

**Dashboard 扩展：**
- `todayTimelineCount` — 今日时间线数量
- `avgTimelineLength` — 平均事件数
- `maxTimelineLength` — 最长事件链
- `avgTimelineDuration` — 平均耗时

**核心设计决策：**
1. **同步增量** — record() 写库后同步追加 Timeline，insert between save and publish
2. **多归属** — 一个 event 可属于多条 Timeline（SPI 遍历）
3. **持续增长 + 会话窗口** — 两种 Timeline 生命周期模式
4. **Story = Timeline** — Story Builder 做排序 + 自动标题，AI 总结留 P8

### Changed — 目录结构重构

前后端分离为独立子项目：

```
core-audit/
├── core-audit-backend/     # Java Spring Boot 后端
│   ├── pom.xml
│   └── src/
├── core-audit-web/          # Vue 3 前端（独立子项目）
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
└── design-docs/             # 设计文档
```

### Changed — Context 字段扩展

- `RequestContext` / `BusinessContext` 新增 `sessionId`、`workflowId`
- `RequestContextProvider` 自动从 HTTP Header `X-Session-Id` / `X-Workflow-Id` 提取
- `ContextResolver` 支持从 metadata 桥接 sessionId / workflowId

### Changed — UI 规范

- 所有 icon 统一使用 FontAwesome，禁止 emoji
- `main.ts` 全局注册 `FontAwesomeIcon` 组件
- `StatCard` 组件改用 FontAwesome 图标

---

## [0.5.0] - 2026-07-17

### Added — P4: Search Runtime

P4 将 `core-audit` 从一个"审计数据存储"升级为**统一审计检索引擎（Audit Query Engine）**，让几百万条审计记录能在几秒内被精准定位。

> 核心目标：**让 Audit 数据真正可查询、可分析、可调查。**

**架构升级：**

```
AuditQuery DSL → Query Planner → AuditQueryEngine → SQL (SQLite/MySQL/Elastic)
                    │
            AuditQueryProvider SPI (5 built-in)
```

**新增核心组件：**

| 组件 | 位置 | 职责 |
|------|------|------|
| `AuditQuery` | `application/domain/query/` | 统一查询 DSL（Builder 模式，30+ 过滤字段） |
| `AuditQueryResult` | `application/domain/query/` | 查询结果包装（分页 + Diff 变更数据） |
| `AuditQueryProvider` | `application/query/spi/` | 可插拔查询条件提供者（SPI 扩展点） |
| `QueryPlan` | `application/query/engine/` | 查询计划（SQL + 参数） |
| `QueryPlanner` | `application/query/engine/` | 查询计划生成器接口 |
| `DefaultQueryPlanner` | `application/query/engine/` | 默认实现（遍历 Provider + Diff JOIN + 排序 + 分页） |
| `AuditQueryEngine` | `application/query/engine/` | 核心引擎入口（JdbcTemplate 执行查询计划） |
| `SortEngine` | `application/query/engine/` | 排序验证与构建（白名单字段，非法回退） |
| `QueryHistoryService` | `application/query/service/` | 搜索历史 + 保存查询管理 |

**5 个内置 AuditQueryProvider（五大搜索范围）：**

| Provider | Order | 覆盖范围 |
|----------|-------|----------|
| `BasicQueryProvider` | 100 | module / action / result / eventType / targetType / targetId |
| `ContextQueryProvider` | 200 | operator / tenant / department / role / ip / uri / browser / os / device / traceId |
| `DiffQueryProvider` | 300 | diffField / diffBefore / diffAfter / diffType → JOIN audit_change |
| `KeywordQueryProvider` | 400 | keyword → LIKE on description + targetId + operatorName |
| `MetadataQueryProvider` | 500 | metadataKey / metadataValue → json_extract on metadata JSON |

**Diff 深度集成：**
- AuditQuery 设置 `diffField` / `diffBefore` / `diffAfter` / `diffType` 时，QueryPlanner 自动生成 `INNER JOIN audit_change` + `SELECT DISTINCT`
- AuditQueryResult 携带 `Map<String, List<Change>>` 返回关联的变更数据

**API 新增（6 个端点）：**

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/v1/audit/query` | 统一审计查询（AuditQuery DSL JSON body） |
| `POST` | `/api/v1/audit/query/save` | 保存查询（name + query + isPublic） |
| `DELETE` | `/api/v1/audit/query/save/{id}` | 删除已保存查询 |
| `GET` | `/api/v1/audit/query/saved?userId=` | 获取用户已保存查询列表（含团队共享） |
| `GET` | `/api/v1/audit/query/recent?userId=` | 获取用户最近搜索历史（最多 20 条） |
| `GET` | `/api/v1/audit/query/popular?limit=` | 获取热门查询（Dashboard 使用） |

**数据库：**
- V5 Flyway 迁移：新增 `audit_saved_query` 表和 `audit_search_history` 表
- 新增复合索引：`idx_ae_module_action_time` (module, action, created_at DESC)、`idx_ae_operator_time` (operator_name, created_at DESC)

**向后兼容：**
- 现有 `GET /api/v1/audit/events` 保持不变，新旧端点并行运行
- `AuditEventService` 新增 `queryViaEngine()` 方法，不影响现有 `query()` 方法

**测试：**
86 个 JUnit 5 用例全部通过（含 21 个新增 P4 测试 + 65 个已有测试兼容）。

---

## [0.4.0] - 2026-07-17

### Added — P3: Diff Runtime

P3 是 `core-audit` 第一次真正具备企业价值的阶段。它不再只记录"谁改了"，而是记录"**到底改了什么**"——字段级的 Before/After 变更对比。

> 核心公式：**Audit + Context + Diff = Enterprise Change Audit**

**新增组件：**

```
Snapshot Resolver → Diff Engine → Change Repository → Audit Event
```

**领域模型：**

| 对象 | 说明 |
|------|------|
| `Snapshot` | 业务对象在某一时刻的完整 JSON 快照（BEFORE / AFTER） |
| `ChangeSet` | 一次变更的完整结构化描述（目标对象 + 所有字段变更 + 操作人 + 时间） |
| `Change` | 单个字段变更（fieldName + changeType + beforeValue + afterValue） |
| `ChangeType` | 枚举：ADD / REMOVE / UPDATE / UNCHANGED |
| `SnapshotType` | 枚举：BEFORE / AFTER |

**Diff Engine — 可插拔策略模式：**

| Strategy | 作用 | 实现方式 |
|----------|------|----------|
| `BeanDiffStrategy` | Java Bean 字段比较 | 反射遍历字段，支持 @AuditIgnore / @AuditDiffField 注解，嵌套对象递归展开 |
| `JsonDiffStrategy` | JSON 节点比较 | Jackson JsonNode 递归遍历，支持嵌套对象点号路径和数组下标路径 |
| `CollectionDiffStrategy` | List / Set 比较 | 按索引比较元素增删改，字段名格式 `items[0]` |
| `MapDiffStrategy` | Map 比较 | 按 key 比较值变化，字段名即 key 名 |

**DiffStrategy 扩展点：**

```java
public interface DiffStrategy<T> {
    boolean supports(Class<?> type);
    ChangeSet compare(T before, T after);
}
```

未来插件可注册 WorkflowDiffStrategy、ConfigDiffStrategy、PermissionDiffStrategy 等自定义策略。

**新注解：**

| 注解 | 作用 |
|------|------|
| `@AuditIgnore` | 标记字段不参与 Diff（如 updateTime、version） |
| `@AuditDiffField` | 自定义字段在 Diff 结果中的显示名称 |

**Diff 生命周期：**

```
业务开始
  ↓
获取 Before Snapshot（业务代码获取旧对象）
  ↓
业务执行（UPDATE）
  ↓
获取 After Snapshot（业务代码获取新对象）
  ↓
SDK.record() → DiffEngine.compare()
  ↓
SnapshotResolver 保存快照 → ChangeRepository 保存变更 → 完成
```

**性能优化：**

- 仅对 `AuditAction.UPDATE` 执行 Diff（CREATE / DELETE 跳过）
- 最大快照大小限制（默认 2MB，通过 `core.audit.diff.max-snapshot-size` 配置）
- `@AuditIgnore` 排除每次必然变更的字段（version、updateTime）
- Diff 失败不阻塞审计记录写入（静默降级）

**数据库：**

- V4 Flyway 迁移：新增 `audit_snapshot` 表和 `audit_change` 表
- `audit_snapshot`：audit_id, snapshot_type (BEFORE/AFTER), content_json (完整对象 JSON)
- `audit_change`：audit_id, field_name, change_type, before_value, after_value
- 3 个索引：`idx_snapshot_audit_id`、`idx_change_audit_id`、`idx_change_field_name`、`idx_change_type`

**API 新增：**

- `GET /api/v1/audit/events/{id}/changes` — 获取指定审计事件的所有字段级变更
- `GET /api/v1/audit/changes/search?field=xxx&after=yyy&page=1&size=20` — 跨事件按字段名和值搜索变更记录

**SDK 增强：**

```java
// 新增带 before/after 的便捷方法
audit.record(AuditModule.USER, AuditAction.UPDATE,
    AuditEventType.USER_UPDATED,
    "User", userId, operatorId, operatorName,
    AuditResult.SUCCESS, "修改用户信息",
    oldUser,  // before
    newUser   // after
);
```

before/after 对象通过 `metadata._before` / `metadata._after` 传递，DiffEngine 在 `record()` 流程中自动检出。

**Dashboard 增强：**

- `GET /api/v1/audit/dashboard` — 新增 `changeTypeDistribution`（今日变更类型分布：ADD/UPDATE/REMOVE 各多少次）、`topChangedFields`（今日变更最多的字段 Top 5）

**配置：**

- `core.audit.diff.enabled` — Diff 功能开关（默认 true）
- `core.audit.diff.max-snapshot-size` — 最大快照大小（默认 2MB）

**P3 不做（留到后续 Phase）：**

- Timeline → P5（依赖多个 Diff 串联）
- Replay → P6（需要请求和响应完整记录）
- AI Change Analysis → P8（基于大量 Diff 数据）
- 合规签名 → P7（与防篡改、电子签名一起实现）
- 多版本对象比较 → P6（需要版本管理支持）
- 自动回滚 → P8（属于智能运维能力）

**测试：**

54 个 JUnit 5 用例全部通过（含 22 个新增 Diff 策略测试 + 8 个现有测试 + 10 个 Repository 集成测试 + 8 个 Service 测试 + 5 个 Controller 测试 + 1 个 Dashboard 测试）。

---

## [0.3.0] - 2026-07-17

### Added — P2: Context Runtime

P2 建立了整个 Core Platform 的**统一上下文模型（Unified Context Model）**，让每条审计事件自动携带完整的业务上下文，无需调用方手动填写。

> 核心原则：**自动采集优先** — 能从 Request/Security/System 获取的信息，绝不让业务重复填写。

**AuditContext 五大上下文：**

| 上下文 | 字段 | 来源 |
|--------|------|------|
| `OperatorContext` | userId, username, organization, role, tenant 等 | SecurityContextProvider（反射访问 Spring Security） |
| `RequestContext` | requestUri, method, traceId, referer, userAgent 等 | RequestContextProvider（RequestContextHolder） |
| `ClientContext` | ip, browser, os, device, language, timezone | ClientContextProvider（简单正则 UA 解析） |
| `BusinessContext` | workspace, project, environment 等 | BusinessContextProvider + metadata 桥接 |
| `SystemContext` | service, version, hostname, thread 等 | SystemContextProvider（Spring Environment） |

**Provider SPI 扩展机制：**

- `AuditContextProvider` 接口（`order()` + `contribute()`），遵循与 `AuditEventSubscriber` 相同的 SPI 模式
- 5 个内置 Provider，按 order 排序执行（100→500）
- `ContextResolver` 编排器：集合注入所有 Provider，故障隔离（单 Provider 失败不阻塞其他）
- 其他 Core 模块可注册自定义 Provider 扩展上下文，无需修改 core-audit

**数据流升级：**

```
SDK.record(event)
  → setDefaults → ContextResolver.resolve(event) → save → publish
```

**数据库：**

- V3 Flyway 迁移：`ALTER TABLE audit_event ADD COLUMN context_json TEXT`
- 自动检测 SQLite/MySQL 方言，使用 `json_extract` / `JSON_EXTRACT`
- 高频过滤字段（module/action/result/time）保持独立列，完整上下文存 JSON

**API 增强：**

- `GET /api/v1/audit/events` — 新增 7 个 Context 过滤参数（tenant, department, browser, ip, workspace, project, traceId）
- `GET /api/v1/audit/events/{id}` — 响应体新增 `context` 字段（五大子上下文完整 JSON）
- `GET /api/v1/audit/events/export` — 支持 Context 参数过滤导出
- `GET /api/v1/audit/dashboard` — 新增 browserDistribution, topOperators, topModules, topOrganizations 统计
- `AuditContextFilter` — 最高优先级 Servlet Filter，MDC traceId 注入

**配置：**

- `core.audit.context.enabled` — 上下文自动采集开关（默认 true）
- `core.audit.context.excluded-paths` — 跳过采集的路径列表

**P2 不做（留到后续 Phase）：**

- Before/After Diff → P3
- Timeline → P5
- Replay → P6
- Geo IP 地图分析 → P8
- AI 异常识别 → P8
- 敏感字段脱敏 → P7
- 多节点 Context 聚合 → P9

**测试：**

23 个 JUnit 5 用例全部通过（含 ContextResolver mock 验证 + Repository 层 context_json 兼容测试）。

---

## [0.2.0] - 2026-07-17

### Added — P1: Audit Event Runtime

P1 将每一条审计记录升级为**可订阅的业务事件（Business Event）**，建立了统一的事件语义和扩展机制。

> 核心原则：**Record First, Event Second** — 审计记录成功落库后再发布事件，保证数据一致性。

**新增能力 (6项)：**

- ✅ `AuditEventType` 枚举 — 24 种独立业务事件类型（USER_CREATED, USER_DELETED, LOGIN_SUCCESS, LOGIN_FAILED, FILE_UPLOADED, WORKFLOW_EXECUTED 等）
- ✅ `EventBus` 接口 — 统一事件总线抽象（publish / subscribe / unsubscribe / getSubscribers），后续可替换为 Kafka / RabbitMQ 而不影响调用方
- ✅ `SpringEventBus` — 默认 JVM 内存实现（基于 Spring ApplicationEventPublisher + 订阅者注册表）
- ✅ `AuditEventPublisher` — 事件发布器，DB 写入成功后自动发布事件
- ✅ `AuditEventSubscriber` 接口 — 其他模块实现此接口即可订阅审计事件
- ✅ `LoggingAuditEventSubscriber` — 内置日志订阅者（监听全部事件，debug 级别）

**领域模型升级：**

`AuditEvent` 新增 8 个 P1 字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `eventId` | String | 事件 ID（UUID，区别于记录 ID） |
| `eventType` | AuditEventType | 业务事件类型 |
| `source` | String | 来源服务名（如 "core-user"） |
| `version` | String | 事件版本，默认 "1.0" |
| `occurredAt` | LocalDateTime | 业务发生时间 |
| `published` | Boolean | 是否发布事件（默认 true） |
| `publishTime` | LocalDateTime | 发布时间 |
| `publishResult` | String | 发布结果（SUCCESS / FAIL） |

**数据库：**

- V2 Flyway 迁移：`ALTER TABLE audit_event` 新增 7 列 + 2 个索引
- 新增 Repository 方法：`countTodayPublished()`, `countTodayPublishFailed()`

**API 增强：**

- `POST /api/v1/audit/events` — 请求体支持 eventType / source / version / publish 字段
- `GET /api/v1/audit/events` — 新增 `eventType` 查询参数过滤
- `GET /api/v1/audit/events/{id}` — 响应体包含全部 P1 字段
- `GET /api/v1/audit/dashboard` — 新增 todayPublished / todayPublishFailed / subscriberCount 统计
- `GET /api/v1/audit/subscribers` — **新增端点**，列出所有已注册的 Event Subscriber

**SDK 增强：**

- `AuditSdkPort` 新增 `record()` 重载方法（带 `AuditEventType` 参数，推荐使用）
- Builder 模式自动推导 `source` 字段

**架构设计：**

```
业务模块 → AuditSdkPort.record()
              ↓
         AuditEventService.record()
              ↓
         Repository.save()  [DB 落库]
              ↓
         AuditEventPublisher.publish()
              ↓
         EventBus.publish()
              ↓
         Subscriber.onEvent()  [同步通知]
```

**P1 不做（留到后续 Phase）：**

- Event Retry / Dead Letter Queue → P6
- Kafka / RabbitMQ / RocketMQ → P9
- Event Schema Registry → P9
- Event Version Migration → P9
- Event Replay → P6
- 分布式事件 → P9

**测试：**

23 个 JUnit 5 用例全部通过（新增 publisher mock 验证，Repository 层 P1 字段兼容测试）。

---

## [0.1.0] - 2026-07-17

### Added — P0 MVP: Audit Runtime

首次发布，实现企业级 Audit Runtime（审计运行时）的基础能力。

**核心能力 (6项)：**
- ✅ `POST /api/v1/audit/events` — 记录审计事件
- ✅ `GET /api/v1/audit/events` — 分页查询（支持 module/action/operator/result/keyword/time 多维过滤）
- ✅ `GET /api/v1/audit/events/{id}` — 查看审计事件详情（含 metadata JSON）
- ✅ `GET /api/v1/audit/events/export` — CSV 导出（BOM 头，Excel 兼容）
- ✅ `GET /api/v1/audit/dashboard` — Dashboard 统计（今日总数/成功/失败/活跃模块数 + 最近操作）
- ✅ `AuditSdkPort` — SDK 接口（异步 + 静默失败，其他 Core 模块调用入口）

**后端 (Spring Boot 3.3.x + Java 17)：**
- 三层架构：API → Application → Infrastructure
- SQLite 默认数据库，Flyway 自动迁移
- 标准化枚举：AuditModule(9) / AuditAction(15) / AuditResult(2)
- 核心领域对象 AuditEvent（14 个审计字段 + JSON metadata 扩展）
- 7 个索引覆盖常见查询场景
- 全局异常处理 + 标准错误响应体
- OpenAPI / Swagger UI 文档
- Actuator 健康检查

**前端 (Vue 3 + Vite + TypeScript)：**
- Dashboard 首页：4 个统计卡片 + 最近操作列表
- 审计记录页：FilterBar（时间/模块/操作/结果/关键字筛选）+ AuditTable + Pagination
- 审计详情页：完整字段展示 + JSON Metadata 可折叠查看
- Apple UI / Bloomberg 极简风格：PillBadge、三级按钮、CSS Variables

**SDK 设计：**
- `AuditSdkPort` 接口 + `AuditSdkImpl` 实现
- `CompletableFuture.runAsync` 异步写入
- 静默失败：写入失败只打 error 日志，绝不抛异常影响业务
- Builder 模式 `default` 方法一键调用

**测试 (21 个 JUnit 5 用例)：**
- Repository 层：10 个用例（CRUD、分页、过滤、关键字搜索、统计）
- Service 层：6 个用例（写入、查询、导出、Dashboard）
- Controller 层：5 个用例（MockMvc：创建、校验、分页、404、Dashboard）

**文档：**
- README.md：项目定位、快速启动、API 端点、数据模型、SDK 使用
- CHANGELOG.md（本文件）

---

### 技术栈

| 层次 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.3.5 |
| Java | 17 |
| 数据库 | SQLite (默认) / MySQL (可选) |
| 数据访问 | Spring JDBC (JdbcTemplate) |
| 迁移 | Flyway |
| API 校验 | Jakarta Validation |
| API 文档 | SpringDoc OpenAPI 2.6.0 |
| CSV | OpenCSV 5.9 |
| 前端 | Vue 3.5 + Vite 6 + TypeScript 5.6 |
| 状态管理 | Pinia 2.2 |
| HTTP 客户端 | Axios 1.7 |
| 测试 | JUnit 5 + Mockito + MockMvc + H2 |

### 端口

- 8108 (默认)

### 未实现（留到后续 Phase）

- Diff（Before/After 对比）→ P3
- Event Bus / 事件发布 → P1
- Timeline 时间线 → P5
- Replay 重放 → P6
- AI 风险分析 → P8
- 多租户隔离 → P7
- 数据保留策略 → P7
- 用户认证/授权（P0 作为内部服务）
