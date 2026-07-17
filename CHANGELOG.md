# CHANGELOG

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
