# DTX Manager ⚡

![java](https://img.shields.io/badge/java-8-blue)
![maven](https://img.shields.io/badge/maven-3.x-brightgreen)
![patterns](https://img.shields.io/badge/patterns-TCC%20%7C%20SAGA%20%7C%202PC-orange)
![demo](https://img.shields.io/badge/demo-order%20%2B%20payment-9cf)

**DTX Manager** 是一个轻量、可嵌入的分布式事务编排组件，内置 **TCC / SAGA / 2PC** 三种模式。项目同时提供可运行的“下单 + 支付 + 库存”演示工程，包含日志埋点、失败重试调度器与 Spring Boot 集成示例。

---

## ✨ 亮点

- **统一入口**：TCC / SAGA / 2PC 一套 API
- **可追踪**：事务记录 + 分支记录结构化存储
- **低侵入**：更换仓库与 ID 生成器即可集成
- **演示完整**：面向新手的可运行 Demo

---

## 🧭 模块结构

```
.
├─ dtx-core    # 核心组件
└─ dtx-demo    # 业务场景演示（订单/库存/支付）
```

---

## 🚀 快速开始

### 1) 构建安装

```bash
mvn -DskipTests=true install
```

### 2) 运行 Demo

```bash
mvn -pl dtx-demo exec:java
```

### 3) 运行 Spring Boot 示例

```bash
mvn -pl dtx-demo spring-boot:run -Dspring-boot.run.profiles=tcc
```

可用 profile：`tcc` / `saga` / `twophase`

---

## 📦 Maven 引入

```xml
<dependency>
    <groupId>io.github.dtxmanager</groupId>
    <artifactId>dtx-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 🧩 最小使用示例

```java
DistributedTransactionEngine engine = new DistributedTransactionEngine();
TccTransactionManager tcc = engine.tcc();

String txId = tcc.execute(participants, attributes);
System.out.println("TCC 事务完成: " + txId);
```

---

## 🔬 设计要点

- **TransactionRecord**：事务主记录（模式 / 状态 / 时间）
- **BranchRecord**：事务分支记录（参与者执行状态）
- **TransactionRepository**：可替换的存储接口

---

## 🧰 Demo 能力清单

- 下单 + 支付 + 库存流程
- 日志埋点示例（关键业务节点）
- 失败重试调度器
- Spring Boot profile 集成

---

## 🛠️ 推荐落地方式

- 实现 `TransactionRepository`：持久化事务记录
- 配置定时扫描：失败自动补偿与重试
- 增加监控告警：提升可观测性

---

## 📄 License

MIT

---

> 注：若你希望使用自己的 GitHub 组织或用户名作为 `groupId`，可替换为 `io.github.<yourname>` 并同步调整包名。
