# DTX Manager ⚡

```
██████╗ ████████╗██╗  ██╗    ███╗   ███╗ █████╗ ███╗   ██╗ █████╗  ██████╗ ███████╗██████╗
██╔══██╗╚══██╔══╝╚██╗██╔╝    ████╗ ████║██╔══██╗████╗  ██║██╔══██╗██╔════╝ ██╔════╝██╔══██╗
██║  ██║   ██║    ╚███╔╝     ██╔████╔██║███████║██╔██╗ ██║███████║██║  ███╗█████╗  ██████╔╝
██║  ██║   ██║    ██╔██╗     ██║╚██╔╝██║██╔══██║██║╚██╗██║██╔══██║██║   ██║██╔══╝  ██╔══██╗
██████╔╝   ██║   ██╔╝ ██╗    ██║ ╚═╝ ██║██║  ██║██║ ╚████║██║  ██║╚██████╔╝███████╗██║  ██║
╚═════╝    ╚═╝   ╚═╝  ╚═╝    ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝
```

![java](https://img.shields.io/badge/java-8-blue)
![maven](https://img.shields.io/badge/maven-3.x-brightgreen)
![patterns](https://img.shields.io/badge/patterns-TCC%20%7C%20SAGA%20%7C%202PC-orange)
![demo](https://img.shields.io/badge/demo-order%20%2B%20payment-9cf)

**DTX Manager** 是一个轻量、可嵌入的分布式事务编排组件，面向 Java 8 项目，内置 **TCC / SAGA / 2PC** 三种模式。项目同时提供可运行的“下单 + 支付 + 库存”演示工程，含日志埋点、失败重试调度器和 Spring Boot 集成示例。

---

## ✨ 特性

- **多模式统一入口**：TCC / SAGA / 2PC 一站式切换
- **事务可追踪**：事务记录 + 分支记录结构化持久化
- **轻量可嵌入**：无侵入设计，低门槛接入
- **演示完整**：新手可读文档 + 可运行 Demo
- **拓展友好**：可替换 ID 生成器与持久化仓库

---

## 🚀 快速开始

### 1) 构建并安装核心组件

```bash
cd C:\code\meets\dtx-manager
mvn -DskipTests=true install
```

### 2) 运行 Demo

```bash
cd C:\code\meets\dtx-manager\demo-order-payment
mvn -q exec:java
```

输出将依次展示 **TCC / SAGA / 2PC** 三种模式的执行结果，以及失败重试调度器演示。

---

## 🧭 项目结构

```
C:\code\meets\dtx-manager
├─ src\main\java\com\meets\dtx        # 核心组件
├─ demo-order-payment                   # 演示工程
│  └─ src\main\java\com\meets\demo
│     ├─ common                         # 领域对象与服务（订单/库存/支付）
│     ├─ tcc                            # TCC 参与者
│     ├─ saga                           # SAGA 步骤
│     ├─ twophase                       # 2PC 参与者
│     ├─ retry                          # 失败重试调度器
│     └─ springboot                     # Spring Boot 集成示例
└─ pom.xml
```

---

## 🧩 核心概念

- **TransactionRecord**：事务的“主记录”，记录事务模式、状态、时间戳等
- **BranchRecord**：事务的“分支记录”，记录每个参与者的执行状态
- **TransactionRepository**：存储接口，支持对接 DB/日志/消息等

---

## 🧪 Demo 模式一览

- **TCC**：Try / Confirm / Cancel
- **SAGA**：Action / Compensate
- **2PC**：Prepare / Commit / Rollback

执行入口：`demo-order-payment/src/main/java/com/meets/demo/DemoApplication.java`

---

## 📈 日志埋点示例

统一使用 `DemoLogger` 进行埋点，示例分布于：

- `demo-order-payment/src/main/java/com/meets/demo/common/*.java`
- `demo-order-payment/src/main/java/com/meets/demo/tcc/*.java`
- `demo-order-payment/src/main/java/com/meets/demo/saga/*.java`
- `demo-order-payment/src/main/java/com/meets/demo/twophase/*.java`

---

## 🔁 失败重试调度器

演示级失败重试调度器：

- `demo-order-payment/src/main/java/com/meets/demo/retry/RetryScheduler.java`

支持：
- 固定退避
- 最大次数控制
- 放弃回调

---

## 🌱 Spring Boot 集成

已内置 Spring Boot 示例，profile 方式运行：

```bash
cd C:\code\meets\dtx-manager\demo-order-payment
mvn spring-boot:run -Dspring-boot.run.profiles=tcc
```

可用 profile：`tcc` / `saga` / `twophase`

入口类：`demo-order-payment/src/main/java/com/meets/demo/springboot/DemoSpringBootApplication.java`

---

## 🛠️ 推荐落地方式

- 实现 `TransactionRepository`，将事务记录写入数据库或日志系统
- 引入定时扫描任务，对失败状态自动补偿或重试
- 接入业务监控和告警，实现端到端可观测

---

## ✅ 当前版本定位

- 组件能力：**可运行、可扩展、可落地**
- 演示能力：**新手友好 + 可直接上手**

---

## 🤝 贡献方式

欢迎基于现有框架补充：

- 业务场景 demo
- 持久化实现
- 重试与恢复策略

---

> 如果你打算发布到私服 / CI/CD 平台，请告诉我你的规范，我可以继续完善发布脚本与版本管理策略。
