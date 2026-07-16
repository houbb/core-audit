P0 解决的是：

> **统一记录（Record）**

而 **P1 的目标不是记录，而是事件（Event）**。

这是整个 `core-audit` 最重要的一次架构升级。

很多系统做到 P0 就结束了，每个模块调用 `auditService.record()` 写数据库。

企业平台不会。

企业平台会认为：

> **每一条 Audit，本质都是一个 Business Event（业务事件）。**

以后：

* Workflow 会监听它
* Notification 会监听它
* AI 会监听它
* 风控会监听它
* Dashboard 会监听它
* Analytics 会监听它

所以 P1 的核心就是：

> **Audit Event Runtime（统一审计事件运行时）**

---

# Phase 1：Audit Event Runtime ⭐⭐⭐⭐⭐

目标：

> **所有审计行为既是一条审计记录，也是一个可订阅的事件。**

一句话：

```text
Record
        ↓
Business Event
        ↓
Subscriber
```

例如：

用户删除

不是：

```text
Delete User
```

而是：

```text
UserDeleted Event
```

以后任何模块都可以订阅。

---

# 为什么要做 Event？

举个例子。

管理员删除了一个用户。

如果没有 Event：

```text
core-user

↓

写 Audit

结束
```

其它系统什么都不知道。

但是如果 Event 化：

```text
core-user

↓

Audit Event

↓

Notification
Workflow
AI
Analytics
Risk
```

所有模块自动响应。

所以：

Audit

开始成为：

整个平台的数据源。

---

# 一、整体架构

建议采用下面这种结构。

```text
                 core-audit

             Audit Controller
                     │
             Audit Service
                     │
          Audit Event Publisher
                     │
        +------------+-------------+
        │            │             │
    Event Store  Local EventBus  Audit Table
        │            │
        │       Event Subscribers
        │
   Analytics / AI / Workflow
```

注意：

MVP 不需要 Kafka。

全部：

内存 EventBus 即可。

以后：

再替换。

---

# 二、Audit 生命周期

建议统一。

```text
业务发生

↓

创建 AuditEvent

↓

写数据库

↓

发布 Event

↓

通知 Subscriber

↓

结束
```

这里：

Record

永远先于：

Publish。

这样：

保证：

Audit

不会丢。

---

# 三、AuditEvent

P0：

AuditEvent

只是：

记录。

P1：

增加：

```text
eventId

eventType

source

version

occurredAt
```

例如：

```text
AuditEvent

id

eventId

eventType

source

module

action

...
```

以后：

支持版本升级。

---

# 四、Event Type

不要：

自己拼字符串。

统一枚举。

例如：

```text
USER_CREATED

USER_UPDATED

USER_DELETED

ROLE_CHANGED

FILE_UPLOADED

FILE_DELETED

CONFIG_UPDATED

LOGIN_SUCCESS

LOGIN_FAILED

API_CALLED

AI_REQUESTED

WORKFLOW_EXECUTED
```

以后：

Workflow：

监听：

```text
USER_CREATED
```

即可。

---

# 五、Event Publisher

新增：

```java
auditPublisher.publish(event);
```

整个流程：

```java
auditService.record(event);

↓

repository.save()

↓

publisher.publish()
```

业务：

不用知道。

---

# 六、Subscriber

新增：

Subscriber。

例如：

```text
AuditSubscriber
```

以后：

所有模块：

自己实现：

```text
AuditEventSubscriber
```

例如：

Notification：

```text
监听：

LOGIN_FAILED
```

收到：

发送短信。

---

Workflow：

监听：

```text
USER_CREATED
```

自动：

创建默认权限。

---

AI：

监听：

```text
DELETE
```

自动：

分析风险。

---

Analytics：

监听：

全部。

统计。

---

# 七、EventBus

建议：

不要：

直接 Spring Event。

而是：

自己封装。

例如：

```text
EventBus
```

接口：

```java
publish()

subscribe()

unsubscribe()
```

为什么？

以后：

替换：

```text
Spring Event

↓

MQ

↓

Kafka

↓

RocketMQ

↓

RabbitMQ
```

业务：

不用改。

---

# 八、事件可靠性

这是 P1 最重要的问题。

建议：

流程：

```text
Save Audit

↓

Commit

↓

Publish Event
```

不要：

先 Publish。

否则：

数据库失败。

事件成功。

数据：

不一致。

---

建议：

事务：

```text
@Transactional

↓

save

↓

commit

↓

publish
```

以后：

可以：

Outbox。

P1：

不用。

---

# 九、后台 UI

新增：

Event 页面。

```text
Audit

Events

Subscribers
```

点击：

Events。

看到：

```text
事件名称

来源模块

发布时间

状态

订阅数量
```

例如：

```text
USER_CREATED

USER

14:00

SUCCESS

4
```

---

点击：

详情。

```text
Event ID

Event Type

Source

Publish Time

Subscribers

Duration

Payload
```

全部：

可见。

---

# 十、Subscriber 页面

例如：

```text
Subscriber

----------------------------------

Notification

Listening

LOGIN_FAILED

USER_CREATED

--------

Workflow

Listening

FILE_UPLOADED

--------

Analytics

Listening

ALL
```

管理员：

一眼知道：

谁：

监听了什么。

---

# 十一、Dashboard

新增：

事件统计。

例如：

```text
今日事件

2048

------------

发布成功

2046

------------

失败

2

------------

Subscriber

12
```

下面：

事件趋势。

即可。

---

# 十二、SDK

新增：

Builder。

例如：

```java
AuditEvent.builder()

.eventType(USER_CREATED)

.module(USER)

.action(CREATE)

.operator(...)

.publish(true)

.build()
```

publish：

默认：

true。

---

# 十三、数据库

P1：

建议：

仍然：

一张：

audit_event。

增加：

几个字段。

| 字段             | 说明           |
| -------------- | ------------ |
| event_id       | 事件ID         |
| event_type     | 事件类型         |
| source         | 来源模块         |
| published      | 是否发布         |
| publish_time   | 发布时间         |
| publish_result | SUCCESS/FAIL |

不用：

Event Table。

保持简单。

---

# 十四、交互设计（UX）

新增左侧导航：

```text
Audit
├── Dashboard
├── Events
├── Subscribers
└── Audit Logs
```

### Event 列表

采用企业后台标准布局：

```text
────────────────────────────────────
事件类型  [__________]
来源模块  [▼]
发布时间  [今天▼]
状态      [▼]

[查询] [重置]

────────────────────────────────────

事件类型      来源      时间      状态      订阅者

USER_CREATED USER   14:00     ✓        3

FILE_DELETE  FILE   14:01     ✓        2

LOGIN_FAILED USER   14:03     ✓        5
```

点击事件进入详情后，顶部展示事件基础信息，下面采用 **Tab** 分区：

```text
------------------------------------------------
USER_CREATED

[Payload] [Subscribers] [Publish History]

------------------------------------------------
```

* **Payload**：结构化 JSON，可折叠查看。
* **Subscribers**：列出监听该事件的模块及处理结果。
* **Publish History**：记录发布时间、耗时、是否成功，便于排查。

整个交互保持与 `core-workflow`、`core-notification` 等模块一致的视觉风格。

---

# 十五、P1 不做的能力（刻意留白）

为了保证 Event Runtime 足够稳定，以下能力建议全部放到后续阶段：

| 能力                          | 放到 Phase | 原因                     |
| --------------------------- | -------- | ---------------------- |
| Event Retry                 | P6       | 与重放机制统一设计              |
| Dead Letter Queue           | P6       | 当前采用本地 EventBus，无需死信队列 |
| Kafka / RabbitMQ / RocketMQ | P9       | 企业部署时按需替换事件总线          |
| Event Schema Registry       | P9       | 大规模事件治理需求              |
| Event Version Migration     | P9       | 跨版本兼容问题留到平台化阶段         |
| Event Replay                | P6       | 依赖完整事件存储和重放框架          |
| 分布式事件                       | P9       | 与企业级集群架构一起演进           |

---

# 十六、P1 的核心设计原则

P1 的目标不是引入复杂的消息中间件，而是**建立统一的事件语义和扩展机制**。建议坚持以下原则：

1. **Record First，Event Second。** 审计记录成功落库后，再发布事件，保证数据一致性。
2. **统一 EventBus 抽象。** 所有模块依赖 `EventBus` 接口，而不是直接依赖 Spring Event 或具体 MQ，实现技术无关。
3. **事件标准化。** `eventType`、`source`、`module` 使用统一枚举，避免字符串混乱。
4. **本地优先。** P1 默认使用 JVM 内本地事件总线，不引入 Redis、Kafka、RabbitMQ 等外部依赖，符合整个 Core Platform 的 MVP 设计理念。
5. **面向未来扩展。** 后续接入 `core-workflow`、`core-notification`、`core-ai`、`core-analytics` 时，只需新增 Subscriber，无需修改 `core-audit` 的核心逻辑。

这样，`Phase 1` 就完成了从**"统一记录系统"**到**"统一事件中心"**的升级，为后续的 Timeline、Replay、Risk Engine、AI RCA 等高级能力奠定了基础。
