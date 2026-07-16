我认为 **P3 是整个 `core-audit` 第一次真正具备企业价值的阶段。**

前面三个阶段：

* **P0**：记录发生了什么（Record）
* **P1**：让记录变成事件（Event）
* **P2**：补充完整上下文（Context）

但是企业真正排查事故时，第一个问题往往不是：

> 谁改了？

而是：

> **到底改了什么？**

因此 **P3 的核心不是 Audit，而是 Change（变更）**。

---

# Phase 3：Diff Runtime ⭐⭐⭐⭐⭐

> Every Change Can Be Compared

目标：

> **记录每一次业务对象变更前后的差异（Before / After），并支持结构化对比。**

一句话：

```text
Audit
    +
Context
    +
Diff
    =
Enterprise Change Audit
```

从这一阶段开始，`core-audit` 不再只是记录"执行了 UPDATE"，而是真正记录：

> **字段 A 从什么值变成了什么值。**

---

# 为什么要做 Diff？

P2 的日志：

```text
echo

UPDATE USER

SUCCESS
```

企业看了以后仍然不知道：

到底修改了什么。

真正希望看到的是：

```text
修改用户：

------------------------------------

Name

echo

↓

echo-admin

------------------------------------

Role

USER

↓

ADMIN

------------------------------------

Status

ENABLE

↓

DISABLED
```

这就是 Diff。

---

# 一、整体架构

建议增加一个专门的 Diff Pipeline。

```text
                 core-audit

               Audit Service
                      │
              Context Resolver
                      │
              Snapshot Resolver
                      │
                Diff Engine
                      │
             Change Repository
                      │
                 Audit Event
```

新增三个核心组件：

```text
Snapshot Resolver

Diff Engine

Change Repository
```

职责清晰。

---

# 二、Diff 生命周期

统一流程建议如下：

```text
业务开始

↓

获取 Before Snapshot

↓

业务执行

↓

获取 After Snapshot

↓

Diff Engine

↓

生成 ChangeSet

↓

保存 Audit
```

注意：

**Before 一定发生在业务修改之前。**

After：

一定发生在事务提交之后。

否则：

Diff 一定会错。

---

# 三、Snapshot（快照）

不要：

直接比较 Entity。

建议：

统一快照对象。

```text
Snapshot
```

例如：

```json
{
  "id":1001,
  "name":"echo",
  "role":"USER",
  "status":"ENABLE"
}
```

修改后：

```json
{
  "id":1001,
  "name":"echo-admin",
  "role":"ADMIN",
  "status":"DISABLED"
}
```

以后：

统一交给：

Diff Engine。

---

# 四、Diff Engine

新增：

```text
DiffEngine
```

统一接口：

```java
ChangeSet compare(
    Snapshot before,
    Snapshot after
);
```

以后：

任何对象：

```text
User

Role

Config

Workflow

Storage

Notification
```

全部：

统一比较。

---

# 五、ChangeSet

这是：

P3 最核心的数据对象。

例如：

```text
ChangeSet
```

里面：

```text
Target

Changes[]

Operator

Time
```

每一个：

Change。

例如：

```text
Field

Before

After
```

例如：

```text
Role

USER

↓

ADMIN
```

以后：

UI：

全部：

消费：

ChangeSet。

---

# 六、字段类型

建议：

统一。

```text
ADD

REMOVE

UPDATE

UNCHANGED
```

例如：

新增：

```text
phone

NULL

↓

138xxxx
```

属于：

```text
ADD
```

删除：

```text
remark

Hello

↓

NULL
```

属于：

REMOVE。

---

# 七、支持复杂对象

企业：

不会：

只有：

String。

例如：

JSON。

```json
{
 "roles":[
   "ADMIN",
   "OPS"
 ]
}
```

修改：

```json
{
 "roles":[
   "ADMIN",
   "OPS",
   "DEV"
 ]
}
```

Diff：

应该：

显示：

```text
roles[]

+ DEV
```

而不是：

整个 JSON 全变了。

所以：

Diff Engine：

支持：

```text
Object

List

Map

JSON
```

以后：

AI：

非常容易分析。

---

# 八、数据库设计

建议新增两张表，而不是把 Diff 塞进 `audit_event`。

## audit_snapshot

保存完整快照。

| 字段            | 说明             |
| ------------- | -------------- |
| id            | 主键             |
| audit_id      | 关联 Audit       |
| snapshot_type | BEFORE / AFTER |
| content_json  | 完整对象快照         |
| created_at    | 时间             |

---

## audit_change

保存结构化变更。

| 字段           | 说明                    |
| ------------ | --------------------- |
| id           | 主键                    |
| audit_id     | 关联 Audit              |
| field_name   | 字段名                   |
| change_type  | ADD / UPDATE / REMOVE |
| before_value | 修改前                   |
| after_value  | 修改后                   |

为什么拆开？

因为：

以后：

查询：

```text
所有修改：

role

字段
```

非常容易。

---

# 九、SDK

建议：

新增：

自动快照。

例如：

```java
audit.record(
    AuditBuilder.update(User.class)
        .before(oldUser)
        .after(newUser)
);
```

业务：

不用：

自己：

Diff。

SDK：

自动：

```text
Snapshot

↓

Diff

↓

Save
```

---

# 十、后台 UI

新增：

Diff Viewer。

例如：

Audit Detail。

顶部：

基本信息。

下面：

新增：

```text
Overview

Context

Changes

Metadata
```

点击：

Changes。

显示：

```text
字段

修改前

修改后

----------------------------------

Name

echo

echo-admin

Role

USER

ADMIN

Status

ENABLE

DISABLED
```

企业：

最喜欢。

---

# 十一、JSON Diff UX

复杂对象。

不要：

文本。

建议：

左右对比。

```text
Before

{

 role:"USER"

}

↓

After

{

 role:"ADMIN"

}
```

字段：

高亮。

颜色：

```text
绿色

新增

黄色

修改

红色

删除
```

以后：

Replay：

直接：

复用。

---

# 十二、搜索 UX

新增：

字段过滤。

例如：

```text
字段：

Role

修改：

USER

↓

ADMIN
```

搜索：

```text
最近7天：

Role

改成：

ADMIN
```

企业：

经常查。

---

# 十三、Dashboard

新增：

变更统计。

例如：

```text
今日：

Change

342

-----------------

新增

88

-----------------

修改

212

-----------------

删除

42
```

下面：

Top：

```text
修改最多：

Config

Role

Workflow
```

非常有价值。

---

# 十四、Diff Strategy（新增扩展点）

不同对象的比较规则不同，因此不要把所有逻辑写死。

建议引入策略接口：

```java
public interface DiffStrategy<T> {

    boolean supports(Class<?> type);

    ChangeSet compare(T before, T after);

}
```

默认提供：

| Strategy               | 作用             |
| ---------------------- | -------------- |
| BeanDiffStrategy       | Java Bean 字段比较 |
| JsonDiffStrategy       | JSON 节点比较      |
| CollectionDiffStrategy | List / Set 比较  |
| MapDiffStrategy        | Map 比较         |

未来插件可以注册：

```text
WorkflowDiffStrategy

ConfigDiffStrategy

PermissionDiffStrategy
```

例如：

权限对象可以忽略字段顺序，只关注权限增减。

---

# 十五、性能优化

Diff 是整个 Audit 中最容易产生性能问题的地方，因此 P3 就要考虑。

建议：

### ① 只对 UPDATE 做 Diff

```text
CREATE

×

DELETE

×

UPDATE

✓
```

---

### ② 大对象限制

例如：

```text
最大：

2MB
```

超过：

只保存：

Snapshot。

不做：

Diff。

---

### ③ Ignore Fields

支持：

```java
@AuditIgnore
private LocalDateTime updateTime;

@AuditIgnore
private Integer version;
```

避免：

每次：

Version

都变化。

---

### ④ Lazy Snapshot

只有：

Audit 开启。

才：

抓取：

Snapshot。

---

# 十六、API

新增：

```text
GET

/api/audit/{id}/changes
```

返回：

```json
[
  {
    "field":"role",
    "before":"USER",
    "after":"ADMIN"
  }
]
```

新增：

```text
GET

/audit/search

?field=role

&after=ADMIN
```

即可。

---

# 十七、P3 不做的能力（刻意留白）

| 能力                 | 放到 Phase | 原因            |
| ------------------ | -------- | ------------- |
| Timeline           | P5       | 依赖多个 Diff 串联  |
| Replay             | P6       | 需要请求和响应完整记录   |
| AI Change Analysis | P8       | 基于大量 Diff 数据  |
| 合规签名               | P7       | 与防篡改、电子签名一起实现 |
| 多版本对象比较            | P6       | 需要版本管理支持      |
| 自动回滚               | P8       | 属于智能运维能力      |

---

# 十八、交互设计（UX）

整个 Diff Viewer 建议采用 IDE 风格，而不是传统表格。

进入详情页：

```text
Audit Detail

────────────────────────────────────

Overview | Context | Changes | Metadata

────────────────────────────────────

Object

User#1001

Operator

echo

Action

UPDATE

────────────────────────────────────

Field          Before            After

Name           echo              echo-admin

Role           USER              ADMIN

Status         ENABLE            DISABLED
```

点击某一行：

右侧展开：

```text
Role

--------------------------------

Before

USER

↓

After

ADMIN
```

如果：

JSON。

则：

左右分栏：

```text
Before                  After

{                       {

 role:"USER"              role:"ADMIN"

}                       }
```

修改字段：

高亮。

未来：

Replay：

直接：

复用这个组件。

---

# 十九、P3 的核心设计原则（最重要）

P3 的目标不是保存两份 JSON，而是建立统一的**变更模型（Unified Change Model）**。

坚持以下原则：

1. **Snapshot 与 Change 分离。** 快照用于完整恢复，Change 用于快速查询和展示。
2. **Diff Engine 独立。** 比较算法封装为独立组件，可替换、可扩展。
3. **结构化优先。** 不保存"修改成功"这样的文本，而保存字段级变化。
4. **插件化策略。** 不同业务对象可以拥有自己的 DiffStrategy，避免一套算法处理所有对象。
5. **为 Timeline、Replay、AI 铺路。** 从这一阶段开始，`core-audit` 不再只是"记录行为"，而是拥有了**对象演化历史**。后续 Timeline 展示的是 ChangeSet 的时间序列，Replay 恢复的是 Snapshot，AI RCA 分析的是 Diff 模式，因此 P3 是整个企业级审计体系中最关键的能力之一。
