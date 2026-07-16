# core-audit

Enterprise Audit Runtime for Core Platform — 统一审计基础设施。

## 定位

> 任何 Core 模块，都能以一行代码完成审计，并且所有审计记录都能在统一界面查看。

**注意**：这不是日志系统（Log），而是**业务审计（Business Audit）**。

| log   | audit    |
| ----- | -------- |
| 调试    | 合规       |
| 程序员看  | 管理员看     |
| 可删除   | 不建议修改    |
| 文本    | 结构化数据    |
| 无业务语义 | 有业务语义    |
| Trace | Business |

## Responsibilities

- 统一审计记录 API（`/api/v1/audit/events`）
- 审计事件分页查询与条件过滤
- Dashboard 统计（今日总数/成功/失败/活跃模块数）
- CSV 导出
- Audit SDK（其他 Core 模块调用入口）

## Non-responsibilities

- Diff（Before/After 对比）— P3
- Event Bus / 事件发布 — P1
- Timeline 时间线 — P5
- Replay 重放 — P6
- AI 风险分析 — P8
- 多租户隔离 — P7
- 数据保留策略 — P7

## 快速启动

```bash
# 克隆
git clone <repo-url>
cd core-audit

# 启动（默认 SQLite）
mvn spring-boot:run

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

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/audit/events` | 写入审计事件 |
| GET | `/api/v1/audit/events` | 分页查询（支持 module/action/operator/result/keyword/time 过滤） |
| GET | `/api/v1/audit/events/{id}` | 查看详情 |
| GET | `/api/v1/audit/events/export` | 导出 CSV |
| GET | `/api/v1/audit/dashboard` | Dashboard 统计 |
| GET | `/actuator/health` | 健康检查 |
| GET | `/swagger-ui/index.html` | OpenAPI 文档 |

### 写入示例

```bash
curl -X POST http://localhost:8108/api/v1/audit/events \
  -H "Content-Type: application/json" \
  -d '{
    "module": "USER",
    "action": "DELETE",
    "targetType": "USER",
    "targetId": "1001",
    "operatorName": "echo",
    "result": "SUCCESS",
    "description": "Delete user"
  }'
```

### 查询示例

```bash
# 分页查询
curl "http://localhost:8108/api/v1/audit/events?page=1&size=20"

# 按模块过滤
curl "http://localhost:8108/api/v1/audit/events?module=USER&action=DELETE"

# 关键字搜索
curl "http://localhost:8108/api/v1/audit/events?keyword=admin"
```

## 数据库

默认使用 SQLite，数据库文件位于 `./data/core-audit.db`。

唯一核心表：`audit_event`

Flyway 自动迁移，版本脚本位于 `src/main/resources/db/migration/`。

## 审计事件模型（AuditEvent）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 事件 ID |
| module | enum | USER/CONFIG/AI/FILE/STORAGE/WORKFLOW/OPENAPI/NOTIFICATION/BILLING |
| action | enum | CREATE/UPDATE/DELETE/LOGIN/LOGOUT/UPLOAD/DOWNLOAD/EXPORT/IMPORT/ENABLE/DISABLE/APPROVE/REJECT/EXECUTE/CALL |
| targetType | string | 对象类型 |
| targetId | string | 对象 ID |
| operatorId | string | 操作人 ID |
| operatorName | string | 操作人名称 |
| result | enum | SUCCESS/FAIL |
| description | string | 操作描述 |
| clientIp | string | 客户端 IP |
| requestUri | string | 请求 URI |
| requestMethod | string | 请求方法 |
| traceId | string | 分布式 Trace ID |
| createdAt | datetime | 审计事件发生时间 |
| metadata | JSON | 扩展字段 |

## SDK 使用

```java
@Autowired
private AuditSdkPort auditSdk;

// Builder 模式
auditSdk.record(
    AuditModule.USER,
    AuditAction.DELETE,
    "USER", "1001",
    currentUser.getId(), currentUser.getName(),
    AuditResult.SUCCESS,
    "Delete user"
);
```

**特性**：
- 异步执行（不阻塞业务线程）
- 静默失败（写入失败只打日志，不抛异常）
- 枚举强类型（编译期杜绝拼写错误）

## 项目结构

```
core-audit/
├── src/main/java/com/github/houbb/core/audit/
│   ├── CoreAuditApplication.java          # 启动类
│   ├── api/                               # API 层
│   │   ├── controller/                    # REST 控制器
│   │   ├── request/                       # 请求 DTO
│   │   ├── response/                      # 响应 DTO
│   │   └── exception/                     # 全局异常处理
│   ├── application/                       # Application 层
│   │   ├── domain/                        # 领域实体 + 枚举
│   │   ├── query/                         # 查询条件
│   │   ├── service/                       # 核心业务服务
│   │   └── port/                          # 仓储接口 + SDK 接口
│   └── infrastructure/                    # Infrastructure 层
│       ├── persistence/                   # JDBC 实现
│       │   ├── entity/                    # DB 实体
│       │   ├── repository/                # JDBC Repository
│       │   ├── jdbc/                      # RowMapper
│       │   └── converter/                 # Entity ↔ Domain 转换
│       ├── sdk/                           # SDK 实现
│       ├── csv/                           # CSV 导出工具
│       └── config/                        # 配置
├── web/                                   # Vue3 前端
└── data/                                  # SQLite 数据 (运行时生成)
```

## 依赖服务

无（core-audit 是基础运行时，不依赖任何其他 Core 模块）。

## 被哪些服务依赖

所有 Core 模块（core-user, core-config, core-storage, core-openapi, core-ai, core-workflow, core-notification, core-billing）通过 Audit SDK 调用 core-audit。

## 技术栈

- Java 17, Spring Boot 3.3.x
- Spring JDBC (JdbcTemplate)
- SQLite (默认) / MySQL (可选)
- Flyway (数据库迁移)
- Vue 3 + Vite + TypeScript (管理后台)
- OpenCSV (CSV 导出)
- JUnit 5 + Mockito (测试)

## License

MIT