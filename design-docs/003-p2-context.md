我会把 **P2 定位为整个 Audit 的"上下文运行时（Context Runtime）"**。

很多系统的 Audit 到 P1 就结束了：

```text
用户删除了一条数据。
```

但是企业真正调查事故时，第一句话不是：

> 删除了什么？

而是：

> **是谁？在哪？通过什么方式？为什么？影响了谁？**

所以 P2 的目标就是：

> **让每一条 Audit Event 都拥有完整的业务上下文（Context）。**

以后 AI、RCA、Timeline、Replay 全都依赖 Context。

---

# Phase 2：Context Runtime ⭐⭐⭐⭐⭐

> Every Audit has Context.

目标：

> **自动采集每一次操作的完整上下文，而不是依赖业务手动填写。**

一句话：

```text
Audit
      +
Context
      =
Enterprise Audit
```

---

# 为什么要做 Context？

P0：

```text
DELETE USER
```

只能知道：

有人删了用户。

但是企业真正需要的是：

```text
2026-07-16 14:25

echo

通过 Web Console

使用 Chrome

IP：10.10.1.25

Tenant：OpenAI

Organization：R&D

Department：Platform

Session：S-123456

Trace：T-987654

删除：

user:1001

耗时：

286ms

结果：

SUCCESS
```

这才是真正的企业审计。

---

# 一、整体架构

```text
                core-audit

               Audit Service
                     │
              Context Resolver
                     │
      +--------------+--------------+
      │              │              │
 RequestContext  SecurityContext  SystemContext
      │              │              │
      +--------------+--------------+
                     │
              Audit Context
                     │
                Audit Event
```

新增：

```text
Context Resolver
```

负责：

自动采集上下文。

业务：

不用管。

---

# 二、Context 自动采集

P2 最大原则：

> **能自动获取的，绝不让业务填写。**

例如：

以前：

```java
audit.record(
    ip,
    browser,
    session,
    trace,
    ...
)
```

以后：

业务：

```java
audit.record(event);
```

剩下：

全部：

Resolver 自动补全。

---

# 三、Context 分类

建议统一划分为五大类。

```text
Operator Context

Request Context

Client Context

Business Context

System Context
```

以后：

所有 Context 都从这里扩展。

---

# 四、Operator Context

表示：

是谁。

例如：

```text
User ID

Username

Nickname

Organization

Department

Role

Permission

Tenant

Login Type
```

例如：

```text
echo

ADMIN

Platform Team

Tenant A
```

以后：

不用业务填写。

直接：

Spring Security 获取。

---

# 五、Request Context

表示：

请求来源。

例如：

```text
Request URI

Method

Trace ID

Request ID

Referer

User Agent

Host

Protocol
```

例如：

```text
POST

/api/user/delete
```

全部：

Filter 自动获取。

---

# 六、Client Context

表示：

客户端。

例如：

```text
IP

Region

Browser

OS

Device

Language

Timezone
```

例如：

```text
Chrome

Windows

Desktop

Asia/Shanghai
```

以后：

Replay

直接使用。

---

# 七、Business Context

这是很多开源项目没有的。

例如：

```text
Module

Resource

Resource ID

Business ID

Workspace

Project

Environment
```

例如：

```text
Project

OpenAI

Workspace

Production
```

以后：

可以：

按业务查询。

---

# 八、System Context

系统信息。

例如：

```text
Service

Node

Version

Hostname

Cluster

Instance

Thread
```

例如：

```text
core-user

v1.2.0

node-01
```

以后：

查线上事故。

非常方便。

---

# 九、Context Resolver

建议：

统一。

不要：

业务：

自己写。

例如：

```text
RequestResolver

↓

SecurityResolver

↓

BusinessResolver

↓

SystemResolver
```

最后：

合并：

```text
AuditContext
```

整个业务：

完全透明。

---

# 十、AuditContext

建议：

不要：

几十个字段。

统一：

对象。

例如：

```text
AuditEvent

↓

context
```

里面：

```text
Operator

Request

Client

Business

System
```

全部：

对象。

以后：

非常容易扩展。

---

# 十一、数据库

P2：

建议：

不要：

增加几十列。

建议：

新增：

```text
context_json
```

例如：

```json
{
  "operator":{

  },
  "request":{

  },
  "client":{

  },
  "business":{

  },
  "system":{

  }
}
```

原因：

以后：

字段：

越来越多。

JSON：

最好。

固定列：

只保留：

```text
module

action

result

time
```

用于查询。

Context：

JSON。

---

# 十二、查询 UX

新增：

高级搜索。

例如：

```text
时间

操作人

组织

租户

模块

操作

结果

IP

浏览器

设备

Trace ID

关键字
```

点击：

高级。

展开：

```text
────────────────────────────

Operator

Organization

Department

Tenant

----------------------------

Request

Trace

URI

Method

----------------------------

Client

Browser

OS

IP

----------------------------

Business

Workspace

Project

```

企业：

非常喜欢。

---

# 十三、详情页 UX

P2：

详情：

升级。

顶部：

基础信息。

下面：

五个 Tab。

```text
Overview

Operator

Request

Client

System
```

例如：

```
Overview
-----------------

DELETE USER

SUCCESS

14:00

```

切换：

Operator：

```
User

echo

Role

ADMIN

Organization

Platform
```

Request：

```
POST

/api/user/delete

Trace

xxxxx
```

体验：

非常舒服。

---

# 十四、Dashboard

新增：

Context 统计。

例如：

```text
今日：

Chrome

68%

Edge

22%

Firefox

10%
```

以及：

```text
Top Operators

Top Modules

Top Organizations
```

全部：

自动统计。

---

# 十五、SDK

业务：

不用变。

依然：

```java
audit.record(event);
```

SDK：

自动：

```text
Resolve Context

↓

Merge

↓

Save
```

业务：

零成本。

---

# 十六、API

查询：

新增：

```text
operator

tenant

department

browser

ip

traceId

workspace

project
```

例如：

```text
GET

/audit/events

?operator=echo

&browser=Chrome

&traceId=xxxx
```

即可。

---

# 十七、Context Provider（新增扩展点）

这里建议增加一个非常重要的扩展机制，而不是把所有上下文解析都写死。

定义统一接口：

```java
public interface AuditContextProvider {

    int order();

    void contribute(AuditContext context);

}
```

默认内置 Provider：

| Provider                | 作用                        |
| ----------------------- | ------------------------- |
| RequestContextProvider  | HTTP 请求信息                 |
| SecurityContextProvider | 当前登录用户                    |
| ClientContextProvider   | IP、UA、设备                  |
| BusinessContextProvider | Module、Resource、Workspace |
| SystemContextProvider   | 服务版本、节点、实例                |

以后 `core-ai`、`core-workflow`、插件系统都可以注册新的 Context Provider，而无需修改 `core-audit`。

这也是企业平台非常常见的设计模式。

---

# 十八、P2 不做的能力（刻意留白）

为了保证 Context Runtime 聚焦于"采集"，以下能力建议放到后续阶段：

| 能力                  | 放到 Phase | 原因                 |
| ------------------- | -------- | ------------------ |
| Before / After Diff | P3       | 属于数据变更模型           |
| Timeline            | P5       | 依赖 Context，但属于关联分析 |
| Replay              | P6       | 需要完整请求与响应数据        |
| Geo IP 地图分析         | P8       | 属于智能分析能力           |
| AI 异常识别             | P8       | 基于大量 Context 数据训练  |
| 敏感字段脱敏              | P7       | 属于合规治理能力           |
| 多节点 Context 聚合      | P9       | 企业级分布式部署           |

---

# 十九、P2 的核心设计原则（最重要）

P2 的价值不在于增加字段，而在于建立统一的上下文模型。

坚持以下原则：

1. **自动采集优先。** 能从 Request、Security、System 获取的信息，不允许业务重复填写。
2. **Context 对象化。** 使用 `AuditContext` 聚合多个子 Context，而不是不断增加数据库字段。
3. **Provider 可扩展。** Context 来源采用 Provider 机制，方便未来插件和各 Core 模块扩展。
4. **查询字段与存储字段分离。** 高频过滤字段保留独立列，完整上下文存放 `context_json`，兼顾性能与扩展性。
5. **为后续能力铺路。** Timeline、Replay、AI RCA、Risk Engine 本质上都是消费 Context 数据，因此 P2 要确保上下文模型稳定、统一、可扩展。

**如果说：**

* **P0** 建立了统一审计记录；
* **P1** 建立了统一事件模型；

那么 **P2** 建立的就是整个 Core Platform 的**统一上下文模型（Unified Context Model）**。从这一阶段开始，`core-audit` 不再只是记录日志，而成为后续 AI 根因分析、操作回放、行为分析、风控和合规能力的基础数据平台。
