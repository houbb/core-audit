# CHANGELOG

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
