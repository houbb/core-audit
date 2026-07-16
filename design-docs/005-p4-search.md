这里我会**重新调整一下 P4 的定位**。

很多系统把 P4 做成"查询页面"，这其实价值不大。

对于企业来说，**Search Runtime 不是一个页面，而是一套统一的审计检索引擎（Audit Query Engine）**。

因为到了 P4，已经拥有了：

* P0：Audit Record（记录）
* P1：Event（事件）
* P2：Context（上下文）
* P3：Diff（变更）

此时数据已经非常丰富。

真正的问题变成：

> **如何在几百万、几千万条 Audit 中，在几秒内找到需要的那一条？**

所以 P4 的核心目标就是：

> **让 Audit 数据真正可查询、可分析、可调查。**

---

# Phase 4：Search Runtime ⭐⭐⭐⭐⭐

> Search Anything.

目标：

> **建立统一的 Audit Query Engine，让任何 Audit 数据都可以快速检索。**

一句话：

```text
Audit
      ↓
Query Engine
      ↓
Search Result
```

企业调查事故，本质就是：

> Search。

---

# 为什么要做 Search Runtime？

假设线上出现事故。

老板问：

```text
昨天晚上

谁

修改了

AI模型配置？
```

如果没有 Search：

开发：

```text
SQL

LIKE

grep

日志

导出Excel
```

查半小时。

企业：

不能接受。

希望：

```text
AI

昨天

UPDATE

CONFIG

SUCCESS
```

3 秒：

出来。

---

# 一、整体架构

建议新增：

```text
                core-audit

              Query Controller
                      │
                Query Service
                      │
          Audit Query Engine
                      │
     +--------+--------+--------+
     │                 │        │
Filter Parser     Query Planner Sort Engine
     │                 │
     +--------+--------+
              │
        Audit Repository
```

新增三个核心组件：

```text
Filter Parser

Query Planner

Sort Engine
```

以后：

Elastic

SQLite

MySQL

全部：

兼容。

---

# 二、Audit Query Engine

这是：

P4 最核心。

统一接口。

例如：

```java
AuditQuery query = AuditQuery.builder()

.module(USER)

.action(DELETE)

.operator("echo")

.result(SUCCESS)

.build();
```

统一：

```java
auditQueryEngine.search(query);
```

以后：

Repository：

不用知道。

---

# 三、AuditQuery

不要：

Controller

直接：

传：

几十个参数。

建议：

统一对象。

例如：

```text
AuditQuery
```

包含：

```text
TimeRange

Module

Action

Operator

Result

Keyword

Context

Diff

Sort

Page
```

以后：

无限扩展。

---

# 四、Filter Parser

负责：

解析：

查询条件。

例如：

用户输入：

```text
昨天

删除

用户
```

Parser：

变成：

```text
Time

Yesterday

Module

USER

Action

DELETE
```

以后：

AI：

直接：

调用。

---

# 五、Query Planner

很多系统：

直接：

拼 SQL。

以后：

越来越乱。

建议：

Planner。

例如：

```text
AuditQuery

↓

Planner

↓

Repository Query
```

以后：

SQLite：

生成：

SQLite SQL。

MySQL：

生成：

MySQL SQL。

以后：

Elastic：

生成：

DSL。

业务：

不用改。

---

# 六、Search Scope

建议：

支持五类搜索。

```text
Basic

Context

Diff

Metadata

Keyword
```

例如：

Basic：

```text
Module

Action

Result
```

Context：

```text
Operator

IP

Tenant

Browser

Trace
```

Diff：

```text
Field

Before

After
```

Metadata：

JSON。

Keyword：

全文。

---

# 七、搜索条件（Filter）

建议统一支持以下维度：

### 基础条件

```text
时间范围
模块
操作
结果
资源类型
资源ID
```

---

### 操作人

```text
User

Role

Organization

Department

Tenant
```

---

### 请求信息

```text
IP

URI

Method

Browser

OS

Device
```

---

### Diff

```text
Field

Before

After

Change Type
```

---

### Event

```text
Event Type

Publisher

Subscriber
```

以后：

企业：

全部：

能查。

---

# 八、排序

统一。

例如：

```text
Time

ASC

DESC

Operator

Module

Duration
```

不要：

写死：

Time DESC。

---

# 九、保存查询（Saved Query）

这是企业非常喜欢的功能。

例如：

管理员：

每天：

查：

```text
删除配置
```

不用：

每次：

重新输。

点击：

```text
Save Search
```

保存：

```text
Production Delete
```

以后：

一键：

打开。

---

数据库：

新增：

```text
audit_saved_query
```

即可。

---

# 十、Recent Search

新增：

最近查询。

例如：

```text
最近搜索

--------------------

DELETE USER

LOGIN FAIL

Role ADMIN

Workflow

Config
```

企业：

非常喜欢。

---

# 十一、Dashboard

新增：

热门查询。

例如：

```text
Top Search

LOGIN

DELETE

CONFIG

AI
```

以后：

分析：

管理员：

关注什么。

---

# 十二、查询结果 UX

建议：

类似：

GitHub。

顶部：

过滤。

中间：

结果。

右边：

详情。

```text
---------------------------------------------------------

Filter

↓

---------------------------------------------------------

Result

↓

---------------------------------------------------------

Detail
```

不用：

一直：

跳页面。

体验：

舒服。

---

# 十三、高级搜索 UX

点击：

Advanced。

展开：

```text
------------------------------------

Time

Module

Operator

Action

Result

------------------------------------

Context

IP

Browser

Trace

------------------------------------

Diff

Field

Before

After

------------------------------------

Metadata

JSON

------------------------------------

```

全部：

折叠。

非常清爽。

---

# 十四、Audit Detail

点击：

一条。

右侧：

Drawer。

显示：

```text
Overview

↓

Context

↓

Changes

↓

Metadata
```

不用：

新页面。

企业：

最喜欢。

---

# 十五、搜索 DSL（新增）

为了避免 Controller 参数越来越多，建议定义统一 DSL。

例如：

```java
AuditQuery.builder()
    .module(USER)
    .action(DELETE)
    .operator("echo")
    .between(start, end)
    .fieldChanged("role")
    .afterValue("ADMIN")
    .traceId("trace-123")
    .build();
```

后续：

AI Agent

直接：

生成 DSL。

不用：

拼 SQL。

---

# 十六、查询优化

P4：

开始：

考虑性能。

建议：

建立：

组合索引。

例如：

```text
module

action

time
```

以及：

```text
operator

time
```

JSON：

不要：

全部：

索引。

只索引：

常查字段。

---

分页：

统一：

```text
LIMIT

OFFSET
```

以后：

MySQL。

SQLite。

一致。

---

# 十七、API

新增：

```text
POST

/api/audit/query
```

Body：

AuditQuery。

而不是：

几十个 QueryParam。

新增：

```text
POST

/api/audit/query/save
```

保存：

查询。

新增：

```text
GET

/api/audit/query/recent
```

最近：

查询。

---

# 十八、查询扩展器（Query Provider）

这里建议增加插件机制。

定义：

```java
public interface AuditQueryProvider {

    boolean supports(String field);

    Specification<?> build(QueryCondition condition);

}
```

例如：

AI：

新增：

```text
Prompt Name
```

Workflow：

新增：

```text
Flow ID
```

不用：

修改：

Query Engine。

插件：

直接：

扩展。

---

# 十九、P4 不做的能力（刻意留白）

| 能力               | 放到 Phase | 原因              |
| ---------------- | -------- | --------------- |
| Timeline         | P5       | 属于事件关联，而不是查询    |
| Replay           | P6       | 查询只是入口，回放属于行为恢复 |
| AI 自然语言搜索        | P8       | 依赖 LLM 理解查询意图   |
| Elasticsearch 集成 | P9       | 企业版按需引入         |
| Graph Query      | P9       | 需要关系图模型         |

---

# 二十、数据库设计

除了已有 `audit_event`、`audit_snapshot`、`audit_change` 外，P4 建议新增：

## audit_saved_query

| 字段         | 说明             |
| ---------- | -------------- |
| id         | 主键             |
| name       | 查询名称           |
| owner_id   | 创建人            |
| query_json | AuditQuery DSL |
| is_public  | 是否共享           |
| created_at | 创建时间           |

未来：

团队可以共享：

```text
生产事故

高风险操作

管理员行为

AI配置修改
```

---

# 二十一、P4 的核心设计原则（最重要）

P4 的本质不是"提供一个搜索框"，而是建立**统一查询模型（Unified Audit Query Model）**。

建议坚持五个原则：

1. **DSL 优先。** 所有查询统一使用 `AuditQuery` 描述，而不是 Controller 拼接参数。
2. **Query Engine 独立。** Repository 不负责解析业务条件，Engine 负责统一规划查询。
3. **可扩展。** 新模块通过 `AuditQueryProvider` 增加查询维度，而不是修改核心代码。
4. **UI 与 DSL 对齐。** 前端高级搜索的每一个过滤项，都能直接映射到 `AuditQuery`，避免前后端规则不一致。
5. **为 AI 做准备。** 后续 `core-ai` 可以直接把自然语言（例如"查昨天所有管理员删除用户的操作"）转换成 `AuditQuery DSL`，Search Runtime 无需修改即可执行。

---

## P0～P4 能力演进总结

```text
P0  Record Runtime
        │
        ▼
统一记录所有审计事件

P1  Event Runtime
        │
        ▼
审计事件可发布、可订阅

P2  Context Runtime
        │
        ▼
自动补全操作上下文

P3  Diff Runtime
        │
        ▼
记录对象变更历史

P4  Search Runtime
        │
        ▼
统一查询与调查引擎
```

到了 **P4**，`core-audit` 已经从一个简单的"操作日志模块"，演进为整个 Core Platform 的**企业级审计调查中心（Audit Investigation Center）**。后面的 **P5 Timeline** 将不再关注单条记录，而是开始把这些记录串联成完整的行为链路，这是审计系统从"数据"走向"洞察"的关键一步。
