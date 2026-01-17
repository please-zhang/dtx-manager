# Demo：下单 + 支付 + 库存（TCC / SAGA / 2PC）

这个 demo 模块演示如何在“下单 + 支付 + 库存”场景下使用三种分布式事务模式：

- TCC
- SAGA
- 2PC

面向新手工程师：按步骤即可跑通。

---

## 1. 运行 Demo

在项目根目录执行：

```bash
mvn -pl dtx-demo exec:java
```

输出将依次展示 **TCC / SAGA / 2PC** 的执行结果，并包含失败重试调度器演示。

---

## 2. 模块结构

```
./dtx-demo/src/main/java/io/github/dtxmanager/demo
├─ common    # 领域对象与服务（订单/库存/支付）
├─ tcc       # TCC 参与者实现
├─ saga      # SAGA 步骤实现
├─ twophase  # 2PC 参与者实现
├─ retry     # 失败重试调度器
├─ springboot# Spring Boot 集成示例
└─ DemoApplication.java
```

---

## 3. 业务设定（简化版）

- **订单服务**：创建订单、确认订单、取消订单
- **库存服务**：预留库存、扣减库存、释放库存
- **支付服务**：冻结资金、扣款、退款

所有数据存放在内存仓库中，便于理解流程。

---

## 4. TCC 说明

TCC 的流程：**Try / Confirm / Cancel**

对应代码：

- `io.github.dtxmanager.demo.tcc.OrderTccParticipant`
- `io.github.dtxmanager.demo.tcc.StockTccParticipant`
- `io.github.dtxmanager.demo.tcc.PaymentTccParticipant`

入口方法：`DemoApplication.runTccDemo()`

---

## 5. SAGA 说明

SAGA 的流程：**Action / Compensate**

对应代码：

- `io.github.dtxmanager.demo.saga.CreateOrderStep`
- `io.github.dtxmanager.demo.saga.ReserveStockStep`
- `io.github.dtxmanager.demo.saga.PayStep`

入口方法：`DemoApplication.runSagaDemo()`

---

## 6. 2PC 说明

2PC 的流程：**Prepare / Commit / Rollback**

对应代码：

- `io.github.dtxmanager.demo.twophase.OrderTwoPhaseParticipant`
- `io.github.dtxmanager.demo.twophase.StockTwoPhaseParticipant`
- `io.github.dtxmanager.demo.twophase.PaymentTwoPhaseParticipant`

入口方法：`DemoApplication.runTwoPhaseDemo()`

---

## 7. 日志埋点示例

统一使用 `DemoLogger` 进行埋点，示例分布于：

- `io.github.dtxmanager.demo.common.*`
- `io.github.dtxmanager.demo.tcc.*`
- `io.github.dtxmanager.demo.saga.*`
- `io.github.dtxmanager.demo.twophase.*`

---

## 8. 失败重试调度器

示例调度器位于：

- `io.github.dtxmanager.demo.retry.RetryScheduler`

演示调用在 `DemoApplication.runRetryDemo()`。

---

## 9. Spring Boot 集成

运行方式：

```bash
mvn -pl dtx-demo spring-boot:run -Dspring-boot.run.profiles=tcc
```

可用 profile：`tcc` / `saga` / `twophase`

关键类：

- 启动类：`io.github.dtxmanager.demo.springboot.DemoSpringBootApplication`
- 配置类：`io.github.dtxmanager.demo.springboot.DemoConfiguration`

---

## 10. 如何模拟失败场景

想观察回滚或补偿，可以故意让库存不足，例如：

- 将 `stockService.initStock("SKU-BOOK", 10)` 改成 `stockService.initStock("SKU-BOOK", 1)`
- 或把 `quantity` 改为 5

然后重新运行即可。

---

## 11. 进一步学习建议

- 实现 `TransactionRepository`，将事务记录写入数据库
- 增加失败重试、补偿重放、定时扫描等机制
- 将参与者实现替换为真实业务服务

