我认为 **P7 是整个 `core-audit` 从"技术平台"迈向"企业平台"的分水岭。**

前面的 P0~P6，更多解决的是：

> **如何记录、查询、分析审计数据。**

而从 P7 开始，关注点变成：

> **如何让这些审计数据满足企业治理、法律法规、内控要求，并且能够证明"这份审计记录可信、不可抵赖、不可篡改"。**

所以我建议把 P7 定位为：

# Phase 7：Compliance Runtime ⭐⭐⭐⭐⭐

> **Trusted Audit for Enterprise**

一句话：

> **让 Audit 从"可以查看"升级为"可以作为企业合规和法律证据使用"。**

---

# 为什么需要 Compliance？

举个例子。

公司发生数据泄露。

监管机构要求：

```text
请提供：

最近180天

所有管理员

删除客户数据

全部记录
```

如果 Audit 可以随便删、

可以随便改、

没有保留策略、

没有签名、

没有脱敏。

那么：

Audit：

没有法律价值。

企业：

也无法通过：

ISO

SOC2

等级保护

金融审计

医疗审计。

所以：

P7

不是功能。

而是：

**Trust（可信）。**

---

# 一、整体架构

建议新增：

```text
                 core-audit

            Compliance Service
                    │
     +--------------+--------------+
     │              │              │
Retention      Integrity      Mask Engine
     │              │              │
     +--------------+--------------+
                    │
          Compliance Repository
                    │
             Audit Repository
```

新增四个核心组件：

```text
Retention Manager

Integrity Engine

Mask Engine

Export Engine
```

---

# 二、Compliance 生命周期

统一流程：

```text
Audit Event

↓

Integrity Sign

↓

Retention Policy

↓

Mask Sensitive Data

↓

Store

↓

Compliance Export
```

注意：

**签名发生在数据入库之前。**

保证：

以后：

任何修改：

都能发现。

---

# 三、Retention Runtime（保留策略）

企业：

最关心。

例如：

```text
Audit

保留：

365 天
```

或者：

```text
管理员操作

永久保留
```

或者：

```text
AI Prompt

90 天
```

全部：

策略化。

---

建议：

Retention Policy。

例如：

```text
Module

Action

Retention

Archive

Delete
```

以后：

不用：

写代码。

---

# 四、Integrity Runtime（完整性）

这是：

企业 Audit

最重要。

目标：

**证明 Audit 没被改过。**

建议：

每条 Audit：

生成：

```text
SHA256 Hash
```

例如：

```text
Audit

↓

Hash

↓

Save
```

以后：

任何字段：

修改。

Hash：

变化。

立即：

发现。

---

进一步：

建议：

Hash Chain。

例如：

```text
Audit1

↓

Hash1

↓

Audit2

↓

Hash2(Hash1)

↓

Audit3

↓

Hash3(Hash2)
```

形成：

链。

以后：

删除：

任何一条。

全部：

断裂。

这就是：

很多金融系统采用的设计。

> **注意**：这里不是为了做区块链，而是借鉴**哈希链（Hash Chain）**思想，实现低成本、高可信的防篡改能力。

---

# 五、Mask Runtime（脱敏）

企业：

不能：

管理员：

看到：

全部数据。

例如：

手机号：

```text
13812345678
```

显示：

```text
138****5678
```

邮箱：

```text
echo@example.com
```

显示：

```text
ec***@example.com
```

身份证：

```text
420**********1234
```

自动：

Mask。

---

建议：

支持：

```java
@AuditMask
```

例如：

```java
@AuditMask(type = PHONE)
private String phone;
```

SDK：

自动：

脱敏。

---

# 六、Sensitive Rule Engine

新增：

规则。

例如：

```text
PHONE

EMAIL

TOKEN

PASSWORD

SECRET

API_KEY
```

以后：

全部：

统一。

不用：

每个模块：

自己：

处理。

---

# 七、Export Runtime

企业：

一定需要。

例如：

导出：

```text
最近一年

管理员

删除用户
```

支持：

```text
CSV

Excel

PDF
```

注意：

导出的内容：

必须带：

签名信息。

例如：

```text
Export Time

Operator

Hash

Signature
```

以后：

作为：

审计附件。

---

# 八、Legal Hold（法律保留）

企业：

经常：

收到：

调查。

例如：

```text
案件：

#2026-001
```

Audit：

不能：

删除。

即使：

Retention：

到期。

也不能。

新增：

```text
Legal Hold
```

即可。

---

# 九、数据库设计

新增：

## audit_retention_policy

| 字段             | 说明   |
| -------------- | ---- |
| id             | 主键   |
| module         | 模块   |
| action         | 操作   |
| retention_days | 保留天数 |
| archive        | 是否归档 |
| enabled        | 启用   |

---

新增：

## audit_signature

| 字段            | 说明       |
| ------------- | -------- |
| audit_id      | 关联 Audit |
| hash          | SHA256   |
| previous_hash | 上一条 Hash |
| algorithm     | 算法       |
| created_at    | 生成时间     |

---

新增：

## audit_legal_hold

| 字段         | 说明       |
| ---------- | -------- |
| id         | 主键       |
| audit_id   | 关联 Audit |
| reason     | 原因       |
| owner      | 负责人      |
| expired_at | 结束时间     |

---

# 十、后台 UX

新增：

Compliance。

```text
Audit

├── Dashboard

├── Timeline

├── Replay

├── Compliance

├── Retention

├── Export
```

---

Retention：

例如：

```text
Module

Retention

Archive

Status

USER

365

YES

ON

CONFIG

730

YES

ON
```

管理员：

直接：

改。

---

# 十一、Integrity Viewer

Audit Detail。

新增：

```text
Integrity
```

显示：

```text
Hash

SHA256

Verified

YES

Chain

OK
```

如果：

Hash：

不一致。

立即：

红色。

```text
Integrity

FAILED
```

---

# 十二、Export UX

点击：

Export。

弹出：

```text
时间范围

模块

格式

脱敏

是否包含 Diff

是否包含 Timeline
```

点击：

生成。

后台：

异步。

完成：

通知。

---

# 十三、Dashboard

新增：

Compliance。

例如：

```text
今日：

Export

8

------------

Retention

1024

------------

Hash Verify

100%

------------

Legal Hold

3
```

企业：

一眼：

知道。

---

# 十四、Compliance Provider（新增扩展点）

不同业务：

保留规则不同。

建议：

插件。

```java
public interface ComplianceProvider {

    boolean supports(Module module);

    CompliancePolicy policy();

}
```

例如：

```text
BillingCompliance

UserCompliance

AiCompliance

WorkflowCompliance
```

全部：

独立。

---

# 十五、API

新增：

```text
GET

/api/compliance/policies
```

新增：

```text
POST

/api/compliance/export
```

新增：

```text
POST

/api/compliance/legal-hold
```

新增：

```text
GET

/api/compliance/hash/{id}
```

全部：

标准化。

---

# 十六、性能设计

Hash：

不要：

查询：

计算。

建议：

入库：

立即：

生成。

Retention：

每天：

Job。

例如：

```text
02:00

Retention Job
```

即可。

Export：

全部：

异步。

不要：

同步。

---

# 十七、交互设计（UX）

建议 Compliance 页面采用企业治理中心布局。

```text
┌────────────────────────────────────────────────────────────┐
│ Compliance Overview                                        │
├──────────────┬────────────────────┬────────────────────────┤
│ Retention    │ Integrity          │ Export                 │
│ Policy List  │ Hash Verification  │ Export History         │
├──────────────┼────────────────────┼────────────────────────┤
│ Legal Hold   │ Sensitive Rules    │ Compliance Reports     │
└──────────────┴────────────────────┴────────────────────────┘
```

点击某条 Audit：

右侧新增：

```text
Overview

Context

Diff

Timeline

Replay

Compliance
```

Compliance：

显示：

```text
Hash

Verified

Retention

365 Days

Mask

Enabled

Legal Hold

No
```

所有企业治理信息：

集中。

---

# 十八、P7 不做的能力（刻意留白）

| 能力        | 放到 Phase | 原因                    |
| --------- | -------- | --------------------- |
| AI 自动合规检查 | P8       | AI 分析属于智能层            |
| 多区域法规模板   | P9       | 企业全球部署                |
| WORM 存储   | P9       | 与对象存储能力结合             |
| 外部监管系统直连  | P9       | 企业集成能力                |
| 区块链存证     | P9       | 极少数行业需要，不作为 Core 默认能力 |

---

# 十九、与前面 Phase 的关系

```text
P0 Record
        │
P1 Event
        │
P2 Context
        │
P3 Diff
        │
P4 Search
        │
P5 Timeline
        │
P6 Replay
        │
P7 Compliance
```

前面：

解决：

**怎么记录。**

P7：

解决：

**如何证明这些记录可信。**

---

# 二十、P7 的核心设计原则（最重要）

P7 的目标不是增加几个导出按钮，而是建立**统一可信审计模型（Trusted Audit Model）**。

建议坚持五个原则：

### ① 数据不可篡改（Tamper Evident）

任何 Audit 被修改，都能立即发现。

不是：

防修改。

而是：

**修改可检测。**

---

### ② 生命周期治理（Lifecycle Governance）

每条 Audit 都有：

```text
生成

↓

保留

↓

归档

↓

删除
```

全部：

策略化。

---

### ③ 默认最小暴露（Privacy by Default）

所有敏感数据：

默认：

Mask。

管理员：

申请：

解密。

不是：

反过来。

---

### ④ 合规能力插件化

Retention、

Mask、

Export、

Compliance Rule

全部：

Provider。

以后：

插件：

直接：

扩展。

---

### ⑤ 为企业认证铺路

从这一阶段开始，`core-audit` 不只是一个开发工具，而是可以支撑：

* ISO 27001
* SOC 2
* 等级保护
* 金融审计
* 医疗审计
* 企业内控

等治理体系。

---

## P0～P7 能力演进

```text
P0  Record Runtime
        │
        ▼
记录行为

P1  Event Runtime
        │
        ▼
传播事件

P2  Context Runtime
        │
        ▼
补全上下文

P3  Diff Runtime
        │
        ▼
记录变更

P4  Search Runtime
        │
        ▼
快速调查

P5  Timeline Runtime
        │
        ▼
重建行为链路

P6  Replay Runtime
        │
        ▼
重建操作过程

P7  Compliance Runtime
        │
        ▼
建立可信、可审计、可合规的企业级审计体系
```

到 **P7**，`core-audit` 已经具备了企业级审计平台最核心的三项能力：

* **可信（Trustworthy）**：哈希链、保留策略、法律保留、脱敏。
* **可调查（Investigable）**：Search、Timeline、Replay。
* **可证明（Provable）**：导出、签名、完整性验证、合规报告。

这也是后续 **P8 Intelligence Runtime** 的基础——AI 不再只是分析日志，而是基于**可信、完整、可追溯**的审计数据进行风险识别、异常检测和 RCA 推理。
