我认为 **core-audit** 是整个 Core Platform 最容易被低估，但长期价值最高的模块之一。

很多开源项目把 Audit 理解成"日志"，实际上企业真正需要的是：

> **Audit = Who + When + Where + What + Why + Before + After + Result**

也就是：

> **任何重要数据，都能回答："是谁，在什么时候，以什么身份，通过什么入口，把什么东西改成了什么，为什么改，最终是否成功。"**

所以，它不是 Log Runtime，而是 **可信审计（Trusted Audit）**。

---

# core-audit

> Enterprise Audit Platform

定位：

> 为整个 Core Platform 提供统一审计能力。

所有模块：

```
core-user
core-config
core-storage
core-openapi
core-ai
core-workflow
core-notification
core-billing
...
```

全部接入。

---

# 为什么放到 Phase 8？

原因很简单。

真正的 Audit 建立在：

```
Identity
✓

Permission
✓

Storage
✓

Workflow
✓

API
✓

AI
✓

Notification
✓
```

之后。

否则：

Audit 连：

是谁

都不知道。

---

# RoadMap

---

# Phase 0

# Audit Runtime ⭐⭐⭐⭐⭐

MVP

目标：

先让所有模块都有统一审计。

一句话：

所有重要操作

都有记录。

例如：

```
创建用户

删除用户

修改角色

上传文件

删除文件

修改配置

发送通知

调用AI

API调用

登录

退出
```

统一：

```
AuditService.record(...)
```

即可。

---

为什么？

很多项目：

每个模块自己打印日志。

最后：

```
grep

grep

grep
```

完全找不到。

所以：

第一阶段：

统一接口。

---

输出：

```
Audit API

Audit Table

Audit UI
```

---

成熟度：

⭐⭐⭐⭐⭐

---

# Phase 1

# Event Runtime ⭐⭐⭐⭐⭐

Audit 不再只是日志。

变成：

事件。

例如：

```
UserCreated

UserDeleted

RoleChanged

FileUploaded

FileDeleted

ConfigUpdated

AIRequested

WorkflowExecuted
```

全部：

Event。

---

为什么？

以后：

很多功能：

都会订阅。

例如：

```
审计

通知

统计

风控

AI

全部消费 Event
```

所以：

Audit

开始事件化。

---

成熟度：

⭐⭐⭐⭐⭐

---

# Phase 2

# Context Runtime ⭐⭐⭐⭐⭐

日志：

必须知道：

上下文。

例如：

```
Operator

Organization

Department

IP

Device

Browser

Session

RequestId

TraceId

API

Latency

Tenant
```

以后：

一条日志：

可以完整定位。

例如：

```
是谁？

哪里？

哪个浏览器？

哪个组织？

哪个租户？

哪个接口？

哪个Trace？

```

---

成熟度：

⭐⭐⭐⭐⭐

---

# Phase 3

# Diff Runtime ⭐⭐⭐⭐⭐

这是企业最喜欢的。

例如：

修改：

```
用户名

echo

↓

echo2
```

Audit：

不是：

```
修改成功
```

而是：

```
Before

echo

After

echo2
```

甚至：

JSON Diff。

例如：

```
Before

{
 role:user
}

↓

After

{
 role:admin
}
```

一眼看出来。

---

成熟度：

⭐⭐⭐⭐⭐

---

# Phase 4

# Search Runtime ⭐⭐⭐⭐⭐

开始支持：

企业搜索。

例如：

搜索：

```
昨天

张三

删除

用户
```

立即：

出来。

支持：

```
过滤

排序

全文

组合条件

导出
```

为什么？

Audit：

真正用途：

查事故。

不是：

存日志。

---

成熟度：

⭐⭐⭐⭐⭐

---

# Phase 5

# Timeline Runtime ⭐⭐⭐⭐⭐

开始：

形成：

时间线。

例如：

一个用户：

```
10:01 登录

↓

10:02 修改配置

↓

10:03 创建AI

↓

10:05 删除文件

↓

10:06 退出
```

全部：

串起来。

企业调查：

就是：

Timeline。

---

成熟度：

⭐⭐⭐⭐⭐

---

# Phase 6

# Replay Runtime ⭐⭐⭐⭐☆

Replay：

企业很喜欢。

例如：

管理员：

修改：

```
Role

Admin
```

Replay：

可以：

重新播放：

整个过程。

例如：

```
点击哪里

修改什么

接口

返回

耗时

结果
```

以后：

Debug：

非常舒服。

甚至：

AI：

可以直接分析。

---

成熟度：

⭐⭐⭐⭐☆

---

# Phase 7

# Compliance Runtime ⭐⭐⭐⭐⭐

开始：

支持：

企业合规。

例如：

GDPR

ISO27001

SOC2

HIPAA

内部审计

支持：

```
Retention

Mask

Encryption

Approval

Export
```

例如：

导出：

```
最近一年：

管理员：

删除数据：

全部记录
```

直接：

PDF。

---

成熟度：

⭐⭐⭐⭐⭐

---

# Phase 8

# Intelligence Runtime ⭐⭐⭐⭐⭐

开始：

AI。

例如：

AI 自动发现：

异常。

例如：

```
凌晨

删除500用户

↓

风险

★★★★★
```

例如：

```
一分钟：

修改配置

30次
```

AI：

直接：

报警。

甚至：

生成：

事故报告。

例如：

```
根因：

管理员误操作

影响：

23个系统

建议：

回滚配置
```

---

成熟度：

⭐⭐⭐⭐⭐

---

# Phase 9

# Enterprise Audit Platform ⭐⭐⭐⭐⭐

最终：

Audit：

变成：

企业平台。

不仅：

记录。

还能：

分析。

还能：

风控。

还能：

AI。

还能：

合规。

最终：

```
                Enterprise Audit Platform

                       Dashboard
                            │
       ┌────────────────────┼────────────────────┐
       │                    │                    │
    Search              Timeline            Investigation
       │                    │                    │
       ├────────────────────┼────────────────────┤
       │                    │                    │
     Diff               Replay              Intelligence
       │                    │                    │
       ├────────────────────┼────────────────────┤
       │                    │                    │
   Compliance          Risk Engine        AI Analysis
       │                    │                    │
       └────────────────────┼────────────────────┘
                            │
                     Audit Runtime API
                            │
 ┌────────┬────────┬────────┬────────┬────────┬────────┐
 │User    │Config  │Storage │AI      │Workflow│OpenAPI │
 └────────┴────────┴────────┴────────┴────────┴────────┘
```

---

# 推荐的整体演进路线

| Phase | 模块                        | 核心目标          | 为什么这一阶段最重要                   |
| ----- | ------------------------- | ------------- | ---------------------------- |
| P0    | Audit Runtime             | 建立统一审计接口与审计记录 | 避免各模块各自记录日志，形成统一标准           |
| P1    | Event Runtime             | 审计事件化         | 为通知、统计、AI、风控等能力提供统一事件源       |
| P2    | Context Runtime           | 丰富上下文信息       | 让每条记录具备完整可追溯性                |
| P3    | Diff Runtime              | 记录变更前后差异      | 从"发生了什么"升级到"具体改了什么"          |
| P4    | Search Runtime            | 高效检索与导出       | 满足事故排查和日常审计需求                |
| P5    | Timeline Runtime          | 构建对象与用户行为时间线  | 将零散事件组织成完整操作链路               |
| P6    | Replay Runtime            | 操作重放          | 提升问题复盘、调试和培训效率               |
| P7    | Compliance Runtime        | 合规与保留策略       | 满足企业级审计、监管和数据治理要求            |
| P8    | Intelligence Runtime      | AI 异常检测与风险分析  | 从记录审计升级为主动发现风险               |
| P9    | Enterprise Audit Platform | 企业级统一审计平台     | 成为整个 Core Platform 的可信行为记录中心 |

## 为什么这样规划？

这条路线遵循了企业审计能力的发展规律：

* **先记录（P0）**：确保所有关键操作都有统一、可靠的审计数据。
* **再丰富（P1～P3）**：加入事件模型、上下文和变更差异，让审计数据真正可理解、可追溯。
* **再利用（P4～P6）**：通过搜索、时间线和重放，将审计数据转化为排障和调查能力。
* **最后智能化（P7～P9）**：结合合规治理、风险分析和 AI，实现从"事后记录"向"事前预警、事中监测、事后分析"的企业级审计平台演进。

对于你的整个 Core Platform（Identity、Config、Storage、Workflow、AI、OpenAPI、Billing 等），**core-audit 应定位为所有核心服务共同依赖的基础运行时（Foundation Runtime）**，而不是一个普通业务模块。它将成为平台可信性、可观测性、合规性和 AI 根因分析（RCA）的统一数据来源。
