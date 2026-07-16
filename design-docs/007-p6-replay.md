这里我建议把 **P6 的定位重新定义一下。**

很多系统把 Replay 理解成：

> 回放日志。

实际上企业真正需要的不是 Replay Log，而是：

> **Replay Operation（操作重放）**

也就是说：

P5 Timeline 回答：

> **发生了什么？**

P6 Replay 回答：

> **我是如何一步一步做到的？**

这是两个完全不同的能力。

对于 RCA（根因分析）、运维、审计来说，Replay 是最有价值的功能之一。

---

# Phase 6：Replay Runtime ⭐⭐⭐⭐⭐

> Reproduce Every Operation.

目标：

> **能够完整重建一次操作过程，支持回放、调查、培训和故障分析。**

一句话：

```text
Audit

↓

Timeline

↓

Replay

↓

Investigation
```

Replay 不再是一条日志。

而是一段完整的操作过程。

---

# 为什么需要 Replay？

例如：

有人删除了一批用户。

Timeline：

只能看到：

```text
14:01

Login

↓

14:02

Delete User

↓

14:03

Logout
```

但是：

企业真正调查时需要知道：

```text
14:01

进入：

用户管理

↓

点击：

批量选择

↓

选择：

235 个用户

↓

点击：

删除

↓

弹出确认框

↓

输入：

DELETE

↓

确认

↓

接口：

POST

/api/users/delete

↓

数据库：

DELETE

↓

Audit

↓

结束
```

这就是：

Replay。

---

# 一、整体架构

建议新增：

```text
                core-audit

               Replay Service
                      │
              Replay Builder
                      │
      +---------------+---------------+
      │               │               │
 Action Recorder  Timeline Loader  Snapshot Loader
      │               │               │
      +---------------+---------------+
                      │
                Replay Session
```

新增：

```text
Replay Builder

Action Recorder

Replay Session
```

---

# 二、Replay 生命周期

统一：

```text
Audit Event

↓

Timeline

↓

Load Snapshot

↓

Build Replay

↓

Replay Session

↓

Viewer
```

Replay：

不是：

重新执行。

而是：

重建过程。

---

# 三、Replay 核心对象

新增：

```text
ReplaySession
```

包含：

```text
Replay ID

Timeline ID

Operator

Start

End

Steps[]
```

里面：

Step：

例如：

```text
Step

Time

Type

Title

Payload
```

以后：

整个 UI：

消费：

ReplaySession。

---

# 四、Replay Step

建议：

统一模型。

例如：

```text
LOGIN

OPEN_PAGE

CLICK_BUTTON

INPUT

REQUEST

DATABASE

AUDIT

EVENT

FINISH
```

Replay：

全部：

Step。

例如：

```text
Step 1

Login
```

↓

```text
Step 2

Open Page
```

↓

```text
Step 3

Delete User
```

↓

结束。

---

# 五、Action Recorder

新增：

Recorder。

负责：

记录：

Replay Step。

来源：

可以：

包括：

```text
Audit Event

Request

Response

Diff

Timeline

Workflow
```

统一：

转成：

ReplayStep。

---

# 六、Replay Builder

统一：

接口。

例如：

```java
ReplaySession build(
    Timeline timeline
);
```

以后：

任何：

Timeline。

都能：

Replay。

---

# 七、Replay Source

建议：

支持：

五类来源。

```text
Audit

Request

Snapshot

Workflow

Event
```

以后：

AI：

也能：

Replay。

---

# 八、数据库设计

新增：

## audit_replay

| 字段          | 说明        |
| ----------- | --------- |
| id          | Replay ID |
| timeline_id | Timeline  |
| title       | 标题        |
| duration    | 耗时        |
| created_at  | 生成时间      |

---

新增：

## audit_replay_step

| 字段           | 说明     |
| ------------ | ------ |
| id           | 主键     |
| replay_id    | Replay |
| sequence     | 顺序     |
| step_type    | 类型     |
| title        | 标题     |
| payload_json | 内容     |

Replay：

以后：

直接：

读取。

不用：

重新生成。

---

# 九、Replay Builder Strategy

不同业务：

Replay：

完全不同。

例如：

Workflow：

Replay：

节点。

用户：

Replay：

按钮。

所以：

建议：

插件。

```java
public interface ReplayStrategy {

    boolean supports(Timeline timeline);

    ReplaySession build(Timeline timeline);

}
```

例如：

```text
UserReplayStrategy

WorkflowReplayStrategy

ApiReplayStrategy

ConfigReplayStrategy
```

全部：

独立。

---

# 十、Replay Player

这是：

整个 UX：

最重要。

建议：

播放器。

例如：

```text
────────────────────────────

▶ Play

⏸ Pause

◀ Prev

Next ▶

Speed

1x

────────────────────────────
```

点击：

播放。

一步一步：

执行。

---

# 十一、时间轴 UX

左侧：

Step。

右侧：

详情。

例如：

```text
○ Login

↓

○ Open User

↓

○ Select

↓

○ Delete

↓

○ Success
```

点击：

Delete。

右侧：

显示：

```text
Request

↓

Diff

↓

Snapshot

↓

Metadata
```

以后：

Debug：

非常舒服。

---

# 十二、快照恢复 UX

例如：

Step：

```text
DELETE USER
```

下面：

增加：

按钮。

```text
Before Snapshot

↓

After Snapshot
```

点击：

左右：

Diff。

不用：

再跳。

---

# 十三、速度控制

播放器：

建议：

支持：

```text
0.5x

1x

2x

4x
```

以后：

培训：

很好用。

---

# 十四、自动定位

点击：

Timeline。

自动：

跳到：

Replay。

例如：

```text
Timeline

↓

Step 8

↓

Replay

↓

Play From Here
```

体验：

很好。

---

# 十五、Dashboard

新增：

Replay。

例如：

```text
今日：

Replay

38

------------

平均

18 Steps

------------

最长

162 Steps

------------

平均耗时

3.2min
```

企业：

调查：

一眼：

知道。

---

# 十六、API

新增：

```text
GET

/api/replay/{id}
```

Replay。

新增：

```text
POST

/api/replay/build
```

生成。

新增：

```text
GET

/api/replay/{id}/steps
```

Step。

新增：

```text
GET

/api/replay/timeline/{id}
```

Timeline：

Replay。

---

# 十七、交互设计（UX）

建议整个页面采用"播放器"布局，而不是传统详情页。

```text
┌─────────────────────────────────────────────────────────────┐
│ Replay Controls                                              │
│ ▶ Play  ⏸ Pause  ⏹ Stop  Speed: [1x▼]                      │
├──────────────┬──────────────────────┬────────────────────────┤
│ Steps        │ Replay Timeline      │ Step Detail            │
├──────────────┼──────────────────────┼────────────────────────┤
│ Login        │ ●                    │ Request                │
│ Open Page    │ │                    │ POST /api/user/delete  │
│ Select User  │ ●                    │                        │
│ Delete       │ │                    │ Diff                   │
│ Confirm      │ ●                    │ USER → ADMIN           │
│ Finish       │                      │                        │
└──────────────┴──────────────────────┴────────────────────────┘
```

特点：

* 左侧：步骤列表，可跳转。
* 中间：时间轴播放，支持暂停、倍速、定位。
* 右侧：当前步骤详情（Request、Context、Diff、Snapshot）。

整个体验更像**视频播放器**，而不是日志查看器。

---

# 十八、性能设计

Replay：

不要：

每次：

重新：

Build。

建议：

第一次：

生成。

以后：

缓存。

例如：

```text
Timeline

↓

Replay Cache

↓

Replay View
```

Replay：

变更。

再：

刷新。

---

# 十九、P6 不做的能力（刻意留白）

| 能力             | 放到 Phase | 原因                              |
| -------------- | -------- | ------------------------------- |
| 真正重新执行业务       | 不建议支持    | Replay 是"重建过程"，不是重新执行，避免误操作生产环境 |
| AI 自动讲解 Replay | P8       | 由 AI 生成操作解说                     |
| 自动回滚           | P8       | Replay 不等于 Rollback，需要独立审批和风控   |
| 跨系统 Replay     | P9       | 企业级统一审计平台再考虑                    |
| 视频录屏           | P9       | Replay 基于结构化数据，而不是屏幕录制          |

---

# 二十、与 Timeline 的关系（最重要）

很多团队容易把 Timeline 和 Replay 混在一起。

建议明确职责：

| 能力       | 回答的问题              | 数据来源                                 |
| -------- | ------------------ | ------------------------------------ |
| Timeline | **发生了哪些事情？**       | Audit Event + Correlation            |
| Replay   | **这些事情是如何一步步完成的？** | Timeline + Snapshot + Diff + Request |

也就是说：

```text
Timeline

09:01 登录

↓

09:03 修改配置

↓

09:05 发布

↓

结束
```

Replay：

```text
Step1

打开：

配置中心

↓

Step2

修改：

AI_MODEL

↓

Step3

点击：

保存

↓

Step4

POST

/config/update

↓

Step5

Workflow

↓

Step6

Audit
```

Replay：

粒度：

远高于：

Timeline。

---

# 二十一、P6 的核心设计原则（最重要）

P6 的定位不是"播放日志"，而是建立**统一操作重建模型（Unified Replay Model）**。

建议坚持以下原则：

1. **Replay 基于结构化数据。** 所有步骤来源于 Audit、Context、Diff、Timeline 等结构化信息，而不是屏幕录像。
2. **Replay 不执行。** 它只负责重建和展示，不负责重新调用接口或修改数据，保证生产环境安全。
3. **插件化构建。** 不同业务（用户、配置、工作流、API）使用不同 `ReplayStrategy`，生成最符合业务语义的步骤。
4. **Timeline 是输入，Replay 是输出。** Timeline 负责事件关联，Replay 负责过程重建，两者职责清晰。
5. **为 AI RCA 铺路。** Replay 提供了完整的操作过程，P8 的 AI 可以直接基于 Replay 生成事故分析、根因解释和优化建议，而无需重新解析底层审计数据。

---

## P0～P6 能力演进

```text
P0  Record Runtime
        │
        ▼
记录发生了什么

P1  Event Runtime
        │
        ▼
事件可以传播

P2  Context Runtime
        │
        ▼
补全操作上下文

P3  Diff Runtime
        │
        ▼
记录对象变化

P4  Search Runtime
        │
        ▼
快速调查定位

P5  Timeline Runtime
        │
        ▼
重建行为链路

P6  Replay Runtime
        │
        ▼
重建完整操作过程
```

到了 **P6**，`core-audit` 已经具备了企业级事故调查平台的核心能力：**不仅知道"发生了什么"，还能完整展示"它是如何一步步发生的"。** 这也是后续 **P7 Compliance Runtime** 和 **P8 Intelligence Runtime** 的重要基础。
