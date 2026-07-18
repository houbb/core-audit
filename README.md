# core-audit

Enterprise Audit Platform — 企业统一审计平台。

## 定位

> 任何 Core 模块，都能以一行代码完成审计，所有审计记录都能在统一界面查看，整个企业共享同一套审计能力。

**注意**：这不是日志系统（Log），而是**业务审计（Business Audit）**。

| log   | audit    |
| ----- | -------- |
| 调试    | 合规       |
| 程序员看  | 管理员看     |
| 可删除   | 不建议修改    |
| 文本    | 结构化数据    |
| 无业务语义 | 有业务语义    |
| Trace | Business |

## P0～P9 能力全景

```
P0  Record Runtime        — 记录行为（统一事件模型 + SQLite/MySQL 双存储）
P1  Event Runtime         — 传播事件（EventBus + Subscriber SPI，异步事件发布）
P2  Context Runtime       — 补全上下文（五大上下文自动采集，无需手动填写）
P3  Diff Runtime          — 记录变更（Before/After 字段级对比，四种 Diff 策略）
P4  Search Runtime        — 快速调查（30+ 过滤字段 DSL，批量查询引擎）
P5  Timeline Runtime      — 重建行为链路（多归属、会话窗口、6 种聚合策略）
P6  Replay Runtime        — 重建操作过程（9 步类型、结构化回放、缓存优先）
P7  Compliance Runtime    — 建立可信治理（SHA-256 哈希链、保留策略、法律保留、脱敏、导出）
P8  Intelligence Runtime  — 智能分析（规则引擎 + 模式识别 + AI Agent SPI + 洞察/建议）
P9  Enterprise Platform   — 企业统一审计平台（SDK 四种模式、多租户、Marketplace、Webhook、Streaming、Global Search）
```

## 核心设计原则

- **One Platform** — 所有系统只有一套 Audit
- **One SDK** — 统一入口，四种写入模式
- **One Timeline** — 跨系统行为串联
- **One Intelligence** — 统一 AI 分析
- **One Governance** — 统一合规治理

## 快速启动

```bash
# 克隆
git clone <repo-url>
cd core-audit

# 启动后端（默认 SQLite）
cd core-audit-backend
mvn spring-boot:run

# 启动前端
cd core-audit-web
npm install
npm run dev

# 访问
# 管理后台: http://localhost:8108/
# Swagger:  http://localhost:8108/swagger-ui/index.html
# Health:   http://localhost:8108/actuator/health
```

### MySQL 模式

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## API 端点

### 核心审计 API（P0～P2）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/audit/events` | 写入审计事件 |
| GET | `/api/v1/audit/events` | 分页查询（支持 module/action/eventType/operator/result/keyword/startTime/endTime/traceId/tenant/department/browser/ip/workspace/project 过滤） |
| GET | `/api/v1/audit/events/{id}` | 查看审计事件详情（含 P1 事件字段 + P2 五大上下文） |
| GET | `/api/v1/audit/events/export` | 导出 CSV（支持 Context 参数过滤） |
| GET | `/api/v1/audit/dashboard` | Dashboard 统计（28 项指标，覆盖 P0～P8） |
| GET | `/api/v1/audit/subscribers` | 列出所有已注册的 Event Subscriber |

### Diff（P3）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/audit/events/{id}/changes` | 获取审计事件的字段级变更 |
| GET | `/api/v1/audit/changes/search` | 按字段名和变更值搜索变更记录（?field=&after=&page=&size=） |

### Search（P4）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/audit/query` | 统一审计查询（AuditQuery DSL JSON body，30+ 过滤字段） |
| POST | `/api/v1/audit/query/save` | 保存查询 |
| GET | `/api/v1/audit/query/saved` | 获取已保存查询列表 |
| GET | `/api/v1/audit/query/recent` | 获取最近搜索历史 |
| GET | `/api/v1/audit/query/popular` | 获取热门查询 |
| DELETE | `/api/v1/audit/query/save/{id}` | 删除已保存查询 |

### Timeline（P5）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/audit/timeline/{id}` | 获取完整 Timeline（含所有事件） |
| GET | `/api/v1/audit/object/{id}/timeline` | 对象生命周期时间线 |
| GET | `/api/v1/audit/operator/{id}/timeline` | 操作人行为时间线 |
| GET | `/api/v1/audit/trace/{traceId}` | 请求链路时间线 |

### Replay（P6）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/audit/replay/{id}` | 获取完整 ReplaySession（含所有步骤） |
| POST | `/api/v1/audit/replay/build` | 构建 Replay（缓存优先） |
| GET | `/api/v1/audit/replay/{id}/steps` | 获取 Replay 步骤列表 |
| GET | `/api/v1/audit/replay/timeline/{timelineId}` | 根据 Timeline 获取 Replay |

### Compliance（P7）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/audit/compliance/overview` | 合规概览 Dashboard 统计 |
| GET | `/api/v1/audit/compliance/policies` | 列出所有保留策略 |
| POST | `/api/v1/audit/compliance/policies` | 创建/更新保留策略 |
| DELETE | `/api/v1/audit/compliance/policies/{id}` | 删除保留策略 |
| GET | `/api/v1/audit/compliance/hash/{auditId}` | 获取签名 + 验证状态 |
| POST | `/api/v1/audit/compliance/verify` | 验证哈希链完整性 |
| GET | `/api/v1/audit/compliance/legal-holds` | 列出所有法律保留 |
| POST | `/api/v1/audit/compliance/legal-hold` | 创建法律保留 |
| DELETE | `/api/v1/audit/compliance/legal-hold/{id}` | 释放法律保留 |
| POST | `/api/v1/audit/compliance/export` | 提交异步导出任务（CSV / EXCEL） |
| GET | `/api/v1/audit/compliance/export/{taskId}` | 查询导出任务状态 |
| GET | `/api/v1/audit/compliance/export/{taskId}/download` | 下载导出文件 |
| GET | `/api/v1/audit/compliance/export/history` | 导出历史列表 |

### Intelligence（P8）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/audit/intelligence/dashboard` | Intelligence Dashboard 统计 |
| GET | `/api/v1/audit/intelligence/insights` | 分页查询 Insights（?page=&size=） |
| GET | `/api/v1/audit/intelligence/insights/{id}` | 获取单个 Insight 详情 |
| GET | `/api/v1/audit/intelligence/risk/{auditId}` | 获取指定 Audit 的风险评分 |
| POST | `/api/v1/audit/intelligence/analyze` | 手动触发分析 |
| GET | `/api/v1/audit/intelligence/recommendations` | 获取全局建议列表（?limit=） |

### Enterprise Platform（P9）

| 方法 | 路径 | 说明 |
|------|------|------|
| **Enterprise Gateway** |||
| GET | `/api/v1/audit/enterprise/overview` | 企业平台总览（聚合所有指标） |
| GET | `/api/v1/audit/enterprise/health` | 平台健康检查 |
| **Source Management** |||
| POST | `/api/v1/audit/enterprise/sources` | 注册/更新来源系统 |
| GET | `/api/v1/audit/enterprise/sources` | 获取所有来源系统 |
| GET | `/api/v1/audit/enterprise/sources/active` | 获取活跃来源 |
| POST | `/api/v1/audit/enterprise/sources/{id}/heartbeat` | 来源心跳 |
| DELETE | `/api/v1/audit/enterprise/sources/{id}` | 删除来源 |
| **Plugin Marketplace** |||
| POST | `/api/v1/audit/enterprise/providers` | 安装插件 |
| GET | `/api/v1/audit/enterprise/providers` | 获取所有插件 |
| GET | `/api/v1/audit/enterprise/providers/active` | 获取活跃插件 |
| GET | `/api/v1/audit/enterprise/providers/type/{providerType}` | 按类型获取插件 |
| PUT | `/api/v1/audit/enterprise/providers/{id}/status` | 启用/禁用插件 |
| DELETE | `/api/v1/audit/enterprise/providers/{id}` | 卸载插件 |
| **Webhook** |||
| POST | `/api/v1/audit/enterprise/subscriptions` | 创建/更新订阅 |
| GET | `/api/v1/audit/enterprise/subscriptions` | 获取所有订阅 |
| GET | `/api/v1/audit/enterprise/subscriptions/enabled` | 获取已启用订阅 |
| DELETE | `/api/v1/audit/enterprise/subscriptions/{id}` | 删除订阅 |
| GET | `/api/v1/audit/enterprise/subscriptions/{id}/deliveries` | 获取投递日志 |
| GET | `/api/v1/audit/enterprise/webhook-deliveries/{auditId}` | 按事件获取投递日志 |
| **Global Search** |||
| GET | `/api/v1/audit/search` | 全局搜索（含 moduleDistribution） |
| GET | `/api/v1/audit/search/suggest` | 搜索建议/快速补全 |

### API 写入示例

```bash
curl -X POST http://localhost:8108/api/v1/audit/events \
  -H "Content-Type: application/json" \
  -d '{
    "module": "USER",
    "action": "DELETE",
    "eventType": "USER_DELETED",
    "targetType": "USER",
    "targetId": "1001",
    "operatorName": "echo",
    "result": "SUCCESS",
    "description": "Delete user"
  }'
```

```bash
# 分页查询
curl "http://localhost:8108/api/v1/audit/events?page=1&size=20"

# 按模块过滤
curl "http://localhost:8108/api/v1/audit/events?module=USER&action=DELETE"

# 关键字搜索
curl "http://localhost:8108/api/v1/audit/events?keyword=admin"

# 全局搜索（P9）
curl "http://localhost:8108/api/v1/audit/search?q=echo&tenant=default"

# 多租户查询（P9）
curl "http://localhost:8108/api/v1/audit/events?tenant=tenant-a"
```

## 审计领域模型（AuditEvent）

| 字段 | 类型 | Phase | 说明 |
|------|------|-------|------|
| id | UUID | P0 | 记录 ID |
| module | enum | P0 | AuditModule — USER/CONFIG/AI/FILE/STORAGE/WORKFLOW/OPENAPI/NOTIFICATION/BILLING |
| action | enum | P0 | AuditAction — CREATE/UPDATE/DELETE/LOGIN/LOGOUT/UPLOAD/DOWNLOAD/EXPORT/IMPORT/ENABLE/DISABLE/APPROVE/REJECT/EXECUTE/CALL |
| eventType | enum | P1 | AuditEventType — USER_CREATED/USER_UPDATED/USER_DELETED 等 24 种 |
| eventId | UUID | P1 | 事件 ID（区别于记录 ID，用于事件路由和去重） |
| source | String | P1 | 来源服务名（如 "core-user"） |
| version | String | P1 | 事件版本（默认 "1.0"） |
| occurredAt | datetime | P1 | 业务发生时间 |
| published | Boolean | P1 | 是否已发布事件 |
| publishTime | datetime | P1 | 发布时间 |
| publishResult | String | P1 | 发布结果（SUCCESS/FAIL） |
| targetType | String | P0 | 目标对象类型 |
| targetId | String | P0 | 目标对象 ID |
| operatorId | String | P0 | 操作人 ID |
| operatorName | String | P0 | 操作人名称 |
| result | enum | P0 | AuditResult — SUCCESS/FAIL |
| description | String | P0 | 操作描述 |
| clientIp | String | P0 | 客户端 IP |
| requestUri | String | P0 | 请求 URI |
| requestMethod | String | P0 | 请求方法 |
| traceId | String | P0 | 分布式 Trace ID |
| createdAt | datetime | P0 | 审计事件发生时间 |
| metadata | JSON | P0 | 扩展信息 |
| context | AuditContext | P2 | 五大上下文（Operator + Request + Client + Business + System） |
| tenant | String | P9 | 多租户标识（默认 "default"，支持 X-Tenant-Id Header 自动注入） |

## 枚举速查

| 枚举 | 值 |
|------|-----|
| **AuditModule** (9) | USER, CONFIG, AI, FILE, STORAGE, WORKFLOW, OPENAPI, NOTIFICATION, BILLING |
| **AuditAction** (15) | CREATE, UPDATE, DELETE, LOGIN, LOGOUT, UPLOAD, DOWNLOAD, EXPORT, IMPORT, ENABLE, DISABLE, APPROVE, REJECT, EXECUTE, CALL |
| **AuditEventType** (24) | USER_CREATED, USER_UPDATED, USER_DELETED, USER_LOGIN_SUCCESS, USER_LOGIN_FAILED, USER_LOGOUT, USER_ENABLED, USER_DISABLED, FILE_UPLOADED, FILE_DELETED, FILE_DOWNLOADED, CONFIG_UPDATED, AI_REQUESTED, WORKFLOW_EXECUTED, WORKFLOW_APPROVED, WORKFLOW_REJECTED, API_CALLED, ROLE_CHANGED, DATA_IMPORTED, DATA_EXPORTED, BILLING_EVENT, STORAGE_OPERATION, NOTIFICATION_SENT |
| **AuditResult** (2) | SUCCESS, FAIL |
| **ChangeType** (4) | ADD, REMOVE, UPDATE, UNCHANGED |
| **TimelineType** (6) | USER, OBJECT, REQUEST, SESSION, INCIDENT, WORKFLOW |
| **ReplayStepType** (9) | LOGIN, OPEN_PAGE, CLICK_BUTTON, INPUT, REQUEST, DATABASE, AUDIT, EVENT, FINISH |
| **RiskLevel** (4) | LOW (0-20), MEDIUM (21-50), HIGH (51-80), CRITICAL (81-100) |
| **SensitiveType** (6) | PHONE, EMAIL, TOKEN, PASSWORD, SECRET, API_KEY |
| **ExportFormat** (3) | CSV, EXCEL, PDF |
| **ExportStatus** (4) | PENDING, PROCESSING, COMPLETED, FAILED |

## SPI 扩展点（10 个可插拔接口）

| 接口 | Phase | 职责 | 内置实现数 |
|------|-------|------|-----------|
| `AuditContextProvider` | P2 | 上下文自动采集（order + contribute） | 5 |
| `DiffStrategy<T>` | P3 | Before/After 对象比较（supports + compare） | 4 |
| `AuditQueryProvider` | P4 | 查询条件提供者（supports + contribute SQL 片段） | 5 |
| `TimelineStrategy` | P5 | Timeline 聚合策略（supports + resolve → TimelineKey） | 6 |
| `ReplayStrategy` | P6 | Replay 构建策略（supports + build → ReplaySession） | 1 |
| `ComplianceProvider` | P7 | 合规保留策略（supports + policy → RetentionPolicy） | — |
| `MaskStrategy` | P7 | 敏感字段脱敏（mask + detectByFieldName） | 1 |
| `AuditAgent` | P8 | AI 分析 Agent（supports + analyze → AuditInsight） | 3 |
| `AuditEventSubscriber` | P1 | 事件订阅者（subscribedTypes + onEvent） | 1 |
| `StreamingAdapter` | P9 | MQ Adapter 抽象（publish + publishBatch + name + isEnabled） | 1 |

**SPI 注册**：所有接口实现类注册为 Spring Bean 后自动被发现和编排，无需手动注册。策略按 `order()` 排序执行，采用故障隔离（单策略失败不影响其他）。

## SDK 使用

```java
@Autowired
private AuditSdkPort auditSdk;

// ① Async — 默认，异步写入不阻塞业务线程（推荐）
auditSdk.record(AuditModule.USER, AuditAction.DELETE,
    AuditEventType.USER_DELETED,
    "USER", "1001",
    currentUser.getId(), currentUser.getName(),
    AuditResult.SUCCESS, "Delete user");

// ② Sync — 同步写入，需要立即获取结果
AuditEvent saved = auditSdk.recordSync(event);

// ③ Batch — 批量同步提交
List<AuditEvent> results = auditSdk.recordBatch(events);

// ④ BatchAsync — 批量异步提交
auditSdk.recordBatchAsync(events);

// P3 Diff — 带 Before/After 对比
auditSdk.record(AuditModule.USER, AuditAction.UPDATE, AuditEventType.USER_UPDATED,
    "User", userId, operatorId, operatorName, AuditResult.SUCCESS,
    "修改用户信息", oldUser, newUser);
```

**特性**：
- 四种写入模式（Sync / Async / Batch / BatchAsync）
- 异步执行（不阻塞业务线程）
- 静默失败（写入失败只打日志，不抛异常，故障隔离）
- 枚举强类型（编译期杜绝拼写错误）
- 多租户自动注入（X-Tenant-Id Header → ThreadLocal → 自动填充）

## 数据库

默认使用 SQLite，数据库文件位于 `./data/core-audit.db`。

Flyway 自动迁移，版本脚本位于 `src/main/resources/db/migration/`：

| 版本 | 说明 | Phase |
|------|------|-------|
| V1 | 创建 audit_event 核心表 | P0 |
| V2 | 新增 event_id、event_type、source 等事件字段 | P1 |
| V3 | 新增 context_json 上下文字段 | P2 |
| V4 | 创建 audit_snapshot、audit_change 表 | P3 |
| V5 | 创建 audit_saved_query、audit_search_history 表 | P4 |
| V6 | 创建 audit_timeline、audit_timeline_event 表 | P5 |
| V7 | 创建 audit_replay、audit_replay_step 表 | P6 |
| V8 | 创建 audit_retention_policy、audit_signature、audit_legal_hold、audit_export_task 表 | P7 |
| V9 | 创建 audit_risk、audit_insight、audit_pattern 表 | P8 |
| V10 | audit_event 加 tenant 列 + audit_source、audit_provider、audit_subscription、audit_webhook_delivery 表 | P9 |

## 前端页面

路由采用 Hash 模式，共 14 个路由：

| 路由 | 页面 | Phase |
|------|------|-------|
| `/` | DashboardPage — 主 Dashboard 首页 | P0 |
| `/events` | AuditListPage — 审计事件列表 | P0 |
| `/events/:id` | AuditDetailPage — 审计事件详情 | P0 |
| `/timeline/:id` | TimelinePage — 三栏时间线详情 | P5 |
| `/object/:id/timeline` | TimelineListPage — 对象时间线 | P5 |
| `/operator/:id/timeline` | TimelineListPage — 操作人时间线 | P5 |
| `/trace/:traceId` | TimelineListPage — 链路时间线 | P5 |
| `/intelligence` | IntelligencePage — 智能分析页 | P8 |
| `/intelligence/insights/:id` | InsightDetailPage — 洞察详情 | P8 |
| `/enterprise` | EnterpriseDashboardPage — 企业运营中心 | P9 |
| `/enterprise/sources` | EnterpriseSourcesPage — 来源系统管理 | P9 |
| `/enterprise/providers` | EnterpriseProvidersPage — 插件市场 | P9 |
| `/enterprise/subscriptions` | EnterpriseSubscriptionsPage — Webhook 管理 | P9 |
| `/enterprise/search` | EnterpriseSearchPage — 全局搜索 | P9 |

## 项目结构

```
core-audit/
├── design-docs/                                     # 设计文档（010-p9-enterprise.md 等）
├── CHANGELOG.md                                     # 变更日志
├── core-audit-backend/                              # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/github/houbb/core/audit/
│       ├── CoreAuditApplication.java                # 启动类 (@EnableScheduling + @EnableAsync)
│       ├── api/                                     # API 层
│       │   ├── controller/                          # 15 个 REST 控制器（62 个端点）
│       │   │   ├── AuditEventController.java        #   P0 审计事件 CRUD + CSV 导出
│       │   │   ├── AuditDashboardController.java    #   P0 Dashboard 统计
│       │   │   ├── SubscriberController.java        #   P1 Subscriber 列表
│       │   │   ├── AuditChangeController.java       #   P3 变更查询
│       │   │   ├── AuditQueryController.java        #   P4 DSL 查询
│       │   │   ├── TimelineController.java          #   P5 Timeline
│       │   │   ├── ReplayController.java            #   P6 Replay
│       │   │   ├── ComplianceController.java        #   P7 Compliance（13 端点）
│       │   │   ├── IntelligenceController.java      #   P8 Intelligence（6 端点）
│       │   │   ├── EnterpriseGatewayController.java #   P9 企业网关
│       │   │   ├── SourceController.java            #   P9 来源管理
│       │   │   ├── MarketplaceController.java       #   P9 插件市场
│       │   │   ├── WebhookController.java           #   P9 Webhook
│       │   │   └── GlobalSearchController.java      #   P9 全局搜索
│       │   ├── request/                             # 请求 DTO
│       │   ├── response/                            # 响应 DTO
│       │   └── exception/                           # 全局异常处理
│       ├── application/                             # Application 层（领域 + 端口 + 服务）
│       │   ├── domain/                              # 领域实体 + 值对象
│       │   │   ├── AuditEvent.java                  #   核心领域对象（Builder 模式，23 字段 + P9 tenant）
│       │   │   ├── enums/                           #   12 个枚举
│       │   │   ├── context/                         #   五大上下文
│       │   │   ├── diff/                            #   Change / ChangeSet / Snapshot
│       │   │   ├── query/                           #   AuditQuery / AuditQueryResult
│       │   │   ├── timeline/                        #   Timeline / TimelineKey / TimelineEvent
│       │   │   ├── replay/                          #   ReplaySession / ReplayStep
│       │   │   ├── compliance/                      #   RetentionPolicy / AuditSignature / LegalHold / ExportTask
│       │   │   ├── intelligence/                    #   AuditRisk / AuditInsight / AuditPattern
│       │   │   └── enterprise/                      #   AuditSource / AuditProvider / AuditSubscription / WebhookDelivery
│       │   ├── port/                                # 仓储端口 + SDK 接口
│       │   │   ├── AuditEventRepository.java        #   审计事件仓储
│       │   │   ├── AuditSdkPort.java                #   SDK 接口（4 种模式）
│       │   │   ├── ChangeRepository.java            #   变更仓储
│       │   │   ├── SnapshotRepository.java          #   快照仓储
│       │   │   ├── SavedQueryRepository.java        #   已保存查询仓储
│       │   │   ├── SearchHistoryRepository.java     #   搜索历史仓储
│       │   │   ├── TimelineRepository.java          #   Timeline 仓储
│       │   │   ├── AuditSourceRepository.java       #   来源系统仓储
│       │   │   ├── AuditProviderRepository.java     #   插件仓储
│       │   │   └── AuditSubscriptionRepository.java #   订阅仓储
│       │   ├── service/                             # 核心业务服务
│       │   │   ├── AuditEventService.java           #   审计事件服务（14 个依赖，record 流水线）
│       │   │   ├── AuditEventPublisher.java         #   事件发布器
│       │   │   ├── DiffEngine.java                  #   Diff 引擎
│       │   │   ├── SnapshotResolver.java            #   快照解析器
│       │   │   ├── TimelineService.java             #   Timeline 服务
│       │   │   ├── ReplayService.java               #   Replay 服务
│       │   │   ├── IntegrityService.java            #   完整性签名服务
│       │   │   ├── MaskService.java                 #   脱敏服务
│       │   │   ├── ComplianceService.java           #   合规调度器
│       │   │   ├── ExportService.java               #   异步导出服务
│       │   │   ├── IntelligenceService.java         #   智能分析服务（Pattern → Risk → AI → Insight → Recommendation）
│       │   │   ├── RuleEngine.java                  #   规则引擎
│       │   │   ├── PatternEngine.java               #   模式引擎
│       │   │   ├── AiAnalyzer.java                  #   AI 分析器
│       │   │   ├── InsightGenerator.java            #   洞察生成器
│       │   │   ├── RecommendationEngine.java        #   建议引擎
│       │   │   ├── SourceService.java               #   来源管理服务
│       │   │   ├── MarketplaceService.java          #   插件市场服务
│       │   │   └── WebhookService.java              #   Webhook 分发服务
│       │   ├── context/                             # 上下文 SPI（AuditContextProvider）
│       │   ├── diff/                                # Diff SPI（DiffStrategy）
│       │   ├── query/spi/                           # 查询 SPI（AuditQueryProvider）
│       │   ├── query/engine/                        # 查询引擎（QueryPlanner + SortEngine + AuditQueryEngine）
│       │   ├── timeline/                            # Timeline SPI（TimelineStrategy）
│       │   ├── replay/                              # Replay SPI（ReplayStrategy）
│       │   ├── compliance/                          # Compliance SPI（ComplianceProvider + MaskStrategy）
│       │   ├── intelligence/                        # Intelligence SPI（AuditAgent）
│       │   ├── event/                               # Event SPI（EventBus + AuditEventSubscriber）
│       │   └── streaming/                           # Streaming SPI（StreamingAdapter）
│       └── infrastructure/                          # Infrastructure 层
│           ├── persistence/                         # JDBC 实现
│           │   ├── entity/                          #   14 个 DB 实体
│           │   ├── repository/                      #   JDBC Repository（SQLite/MySQL 双方言）
│           │   ├── jdbc/                            #   RowMapper
│           │   └── converter/                       #   Entity ↔ Domain 转换器
│           ├── sdk/                                 # SDK 实现（AuditSdkImpl）
│           ├── event/                               # SpringEventBus + LoggingSubscriber
│           ├── context/                             # 5 个 ContextProvider 实现
│           ├── config/                              # AuditProperties + WebMvcConfig
│           ├── csv/                                 # CsvExportUtil
│           ├── timeline/                            # 6 个 TimelineStrategy 实现
│           ├── replay/                              # DefaultReplayStrategy
│           ├── compliance/                          # DefaultMaskStrategy + ComplianceScheduler
│           ├── intelligence/                        # RiskAgent / SecurityAgent / BehaviorAgent
│           ├── streaming/                           # LocalStreamingAdapter
│           └── tenant/                              # TenantFilter
├── core-audit-web/                                  # Vue 3 前端
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── api/                                     # API client（audit.ts + enterprise.ts）
│       ├── components/                              # 通用组件（StatCard, AuditTable, FilterBar, PillBadge, Pagination, TimelineNode, JsonViewer）
│       ├── pages/                                   # 14 个页面
│       ├── types/                                   # TypeScript 类型定义
│       └── router/                                  # Hash 路由（14 条）
└── data/                                            # SQLite 数据（运行时生成）
```

## 依赖服务

无（core-audit 是基础运行时，不依赖任何其他 Core 模块）。

## 被哪些服务依赖

所有 Core 模块（core-user, core-config, core-storage, core-openapi, core-ai, core-workflow, core-notification, core-billing）通过 Audit SDK 调用 core-audit。此外：

- `core-ai` 基于 Audit 进行推理
- `core-workflow` 基于 Audit 触发自动化
- `core-notification` 基于 Audit 发送风险通知
- `core-openapi` 基于 Audit 开放审计接口
- AI RCA 平台基于 Audit 完成根因分析

## 配置

```yaml
core:
  audit:
    context:
      enabled: true                       # P2 上下文自动采集
    diff:
      enabled: true                       # P3 Diff
      max-snapshot-size: 2097152          # 最大快照大小（2MB）
    replay:
      enabled: true                       # P6 Replay
    compliance:
      enabled: true                       # P7 合规
      integrity:
        algorithm: SHA-256                # 哈希算法
      mask:
        enabled: true                     # 脱敏
    intelligence:
      enabled: true                       # P8 智能分析
      rule:
        enabled: true                     # 规则引擎
      ai:
        enabled: false                    # AI 分析（默认关闭，需配置 API Key）
    enterprise:
      enabled: true                       # P9 企业平台
      default-tenant: default             # 默认租户
      webhook-enabled: true               # Webhook
      streaming-enabled: true             # Streaming
      marketplace-enabled: true           # Marketplace
```

## 技术栈

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
| Excel | Apache POI 5.2.5 |
| 前端 | Vue 3.5 + Vite 6 + TypeScript 5.6 |
| 状态管理 | Pinia 2.2 |
| HTTP 客户端 | Axios 1.7 |
| 测试 | JUnit 5 + Mockito + MockMvc + H2 |
| 端口 | 8108 (默认) |

## 测试

149 个 JUnit 5 用例全部通过，0 失败（覆盖 P0～P9 全部模块）。

## License

MIT