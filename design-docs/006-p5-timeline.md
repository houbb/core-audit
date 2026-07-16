这里我建议对 **P5 的定位做一次升级**。

很多产品把 Timeline 理解成：

> 按时间排序的日志。

这是不够的。

企业真正需要的 Timeline 是：

> **围绕一个对象、一名用户、一个请求、一场事故，重建完整的行为轨迹（Behavior Timeline）。**

也就是说：

P4 查到的是：

```text
14:01 修改配置

14:05 删除用户

14:08 登录
```

P5 要做到的是：

```text
事故 #INC-20260716

14:01 登录后台
        │
14:02 修改 AI 配置
        │
14:03 Workflow 自动发布
        │
14:04 API 开始报错
        │
14:05 用户大量失败
        │
14:08 管理员回滚配置
        │
14:10 系统恢复
```

这才是真正的 Timeline。

---

# Phase 5：Timeline Runtime ⭐⭐⭐⭐⭐

> Rebuild Every Story.

目标：

> **把离散的 Audit Event 串联成一条完整的行为时间线。**

一句话：

```text
Audit Event

↓

Timeline Engine

↓

Story
```

从这一阶段开始，

Audit 不再是一条条日志。

而是：

**一个完整故事（Story）。**

---

# 为什么需要 Timeline？

假设：

用户投诉：

```text
我的权限为什么突然没有了？
```

P4：

只能查：

```text
DELETE ROLE

UPDATE USER

LOGIN
```

但是：

真正需要的是：

```text
09:01

管理员登录

↓

09:03

修改角色

↓

09:04

Workflow 自动同步

↓

09:05

权限缓存刷新

↓

09:06

用户重新登录

↓

09:07

权限消失
```

企业调查：

都是：

Timeline。

---

# 一、整体架构

新增：

```text
                core-audit

             Timeline Service
                     │
             Timeline Engine
                     │
      +--------------+--------------+
      │              │              │
Correlation     Event Merge     Story Builder
      │              │              │
      +--------------+--------------+
                     │
               Timeline Repository
```

新增三个核心组件：

```text
Correlation Engine

Event Merge

Story Builder
```

---

# 二、Timeline 生命周期

建议统一流程：

```text
Audit Event

↓

Correlation

↓

Timeline Builder

↓

Timeline

↓

Timeline View
```

不是：

查询的时候：

临时拼。

而是：

生成：

Timeline。

---

# 三、Timeline 核心对象

新增：

```text
Timeline
```

包含：

```text
Timeline ID

Timeline Type

Title

Start Time

End Time

Events[]

Duration
```

例如：

```text
Timeline

User Permission Changed
```

里面：

包含：

```text
LOGIN

UPDATE ROLE

CACHE REFRESH

LOGOUT
```

全部：

关联。

---

# 四、Timeline Type

建议：

统一分类。

例如：

```text
User Timeline

Object Timeline

Request Timeline

Session Timeline

Incident Timeline

Workflow Timeline
```

以后：

AI：

直接：

消费。

---

# 五、Correlation Engine

这是：

P5 的灵魂。

负责：

判断：

哪些 Event：

属于：

同一条 Timeline。

例如：

依据：

```text
Trace ID

Request ID

Session ID

Operator

Resource ID

Workflow ID

Tenant
```

自动：

聚合。

例如：

同一个：

Trace。

全部：

放一起。

---

# 六、Story Builder

不是：

简单排序。

而是：

生成：

故事。

例如：

```text
User Login

↓

Create Workflow

↓

Workflow Execute

↓

Notification Send
```

Story：

已经：

完整。

以后：

AI：

总结：

一句话：

```text
管理员创建 Workflow 后自动发送通知。
```

---

# 七、Timeline Repository

建议新增：

## audit_timeline

| 字段         | 说明          |
| ---------- | ----------- |
| id         | Timeline ID |
| type       | 类型          |
| title      | 标题          |
| start_time | 开始          |
| end_time   | 结束          |
| duration   | 耗时          |
| summary    | 摘要          |

---

## audit_timeline_event

| 字段          | 说明          |
| ----------- | ----------- |
| timeline_id | 关联 Timeline |
| audit_id    | Audit Event |
| sequence    | 顺序          |

这样：

查询：

非常快。

不用：

每次：

Join。

---

# 八、Timeline Builder

建议：

采用：

Builder。

例如：

```java
Timeline.builder()

.type(USER)

.title("User Updated")

.add(event)

.add(event)

.build();
```

以后：

插件：

也能：

构建。

---

# 九、Timeline UI

左侧：

Timeline。

右侧：

详情。

例如：

```text
──────────────────────────────

Timeline

○ Login

│

○ Update Role

│

○ Refresh Cache

│

○ Logout

──────────────────────────────
```

点击：

节点。

右侧：

显示：

```text
Audit Detail

↓

Context

↓

Diff

↓

Metadata
```

体验：

类似：

GitHub Commit。

---

# 十、对象时间线（Object Timeline）

例如：

用户：

```text
User#1001
```

点击：

Timeline。

看到：

```text
2026-01-01

Create

↓

Role Change

↓

Department Change

↓

Disable

↓

Enable

↓

Delete
```

整个生命周期。

全部：

可见。

---

# 十一、用户时间线（Operator Timeline）

例如：

管理员：

```text
echo
```

点击：

Timeline。

显示：

```text
Login

↓

Update Config

↓

Delete File

↓

Approve Workflow

↓

Logout
```

企业：

最喜欢。

---

# 十二、请求时间线（Trace Timeline）

依据：

Trace ID。

例如：

```text
POST

/api/user/update

↓

User Service

↓

Workflow

↓

Notification

↓

Audit
```

整个调用：

全部：

串起来。

以后：

RCA。

直接：

分析。

---

# 十三、Dashboard

新增：

Timeline。

例如：

```text
今日：

Timeline

328

------------

平均长度

12 Events

------------

最长

86 Events

------------

平均耗时

2.8s
```

下面：

Top Timeline。

---

# 十四、搜索 UX

新增：

Timeline。

例如：

```text
Search

↓

Timeline

↓

Story
```

输入：

```text
User1001
```

直接：

Timeline。

不是：

100 条日志。

---

# 十五、详情 UX

顶部：

```text
Timeline

↓

Overview

↓

Events

↓

Graph

↓

Metadata
```

Graph：

例如：

```text
Login

↓

Update

↓

Workflow

↓

Notification
```

比：

表格：

舒服。

---

# 十六、Graph View（新增）

建议：

除了时间轴，

增加：

关系图。

例如：

```text
Operator

↓

User

↓

Workflow

↓

Notification
```

形成：

Graph。

以后：

AI：

非常容易分析。

---

# 十七、Timeline Strategy（新增扩展点）

不同业务对"关联"的规则不同，因此建议提供策略接口。

```java
public interface TimelineStrategy {

    boolean supports(AuditEvent event);

    TimelineKey resolve(AuditEvent event);

}
```

默认实现：

| Strategy                 | 聚合依据        |
| ------------------------ | ----------- |
| TraceTimelineStrategy    | Trace ID    |
| SessionTimelineStrategy  | Session ID  |
| ResourceTimelineStrategy | Resource ID |
| OperatorTimelineStrategy | Operator    |
| WorkflowTimelineStrategy | Workflow ID |

以后：

`core-ai`、`core-workflow` 可以新增自己的 Timeline。

---

# 十八、API

新增：

```text
GET

/api/audit/timeline/{id}
```

返回：

Timeline。

新增：

```text
GET

/api/audit/object/{id}/timeline
```

对象：

生命周期。

新增：

```text
GET

/api/audit/operator/{id}/timeline
```

管理员：

Timeline。

新增：

```text
GET

/api/audit/trace/{traceId}
```

Trace：

Timeline。

---

# 十九、性能设计

Timeline：

不能：

实时：

全库扫描。

建议：

事件写入：

同时：

更新：

Timeline。

例如：

```text
Audit Save

↓

Timeline Append

↓

Done
```

不要：

查询：

临时：

Merge。

性能：

差。

---

建议：

Timeline：

缓存：

最近：

1000。

后面：

MySQL。

即可。

---

# 二十、P5 不做的能力（刻意留白）

| 能力                  | 放到 Phase | 原因                               |
| ------------------- | -------- | -------------------------------- |
| Replay              | P6       | Timeline 是"发生了什么"，Replay 是"如何发生" |
| AI Timeline Summary | P8       | 依赖 LLM 自动总结                      |
| Root Cause Graph    | P8       | 属于智能分析                           |
| 分布式 Timeline 聚合     | P9       | 企业多节点部署                          |
| 时序数据库               | P9       | 数据规模更大时再考虑                       |

---

# 二十一、交互设计（UX）

整个 Timeline 页面建议采用三栏布局，这是企业调查场景效率最高的形式。

```text
┌────────────────────────────────────────────────────────────┐
│ Timeline List │ Time Axis │ Event Detail                  │
├───────────────┼───────────┼───────────────────────────────┤
│ User#1001     │ ○ Login   │ Operator                      │
│ Incident#12   │ │         │ echo                          │
│ Trace#ABC     │ ○ Update  │                              │
│ Workflow#88   │ │         │ Diff                         │
│               │ ○ Notify  │ Role: USER → ADMIN           │
│               │ │         │                              │
│               │ ○ Finish  │ Context                      │
│               │           │ Trace: xxx                   │
└───────────────┴───────────┴───────────────────────────────┘
```

交互特点：

* 左侧选择 Timeline（按对象、用户、事故、Trace 等）。
* 中间展示时间轴，可缩放、折叠、跳转到任意事件。
* 右侧展示当前事件详情，直接复用 P2（Context）和 P3（Diff）的组件。

整个页面无需频繁跳转，调查效率远高于传统分页表格。

---

# 二十二、P5 的核心设计原则（最重要）

P5 的目标不是把日志按时间排序，而是建立**统一行为链路模型（Unified Timeline Model）**。

坚持以下原则：

1. **Timeline 是聚合结果，不是日志列表。** 一个 Timeline 包含多个 Audit Event，表达一次完整业务过程。
2. **Correlation 独立。** 使用 `TimelineStrategy` 决定事件归属，避免业务代码写关联逻辑。
3. **增量维护。** 新事件写入时更新 Timeline，而不是查询时全量拼装，保证性能。
4. **多视角统一。** 同一批 Audit Event 可以形成不同 Timeline：按用户、按对象、按 Trace、按 Workflow、按事故。
5. **为 Replay 和 AI 铺路。** Timeline 回答的是"事情是如何发展的"，Replay 将回答"每一步具体发生了什么"，AI RCA 将回答"为什么会这样发展"。

---

## P0～P5 能力演进

```text
P0  Record Runtime
        │
        ▼
统一记录

P1  Event Runtime
        │
        ▼
统一事件

P2  Context Runtime
        │
        ▼
统一上下文

P3  Diff Runtime
        │
        ▼
统一变更

P4  Search Runtime
        │
        ▼
统一查询

P5  Timeline Runtime
        │
        ▼
统一行为链路
```

到 **P5**，`core-audit` 已经从"记录操作"升级为**重建业务行为过程**。这也是后续 **P6 Replay Runtime** 的基础：Timeline 告诉你**发生了哪些步骤**，Replay 则进一步告诉你**这些步骤是如何一步步执行的**。
