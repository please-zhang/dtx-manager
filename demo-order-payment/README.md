# Demo：下单 + 支付 + 库存（TCC / SAGA / 2PC）

这个 demo 工程演示如何在“下单 + 支付 + 库存”场景下，使用分布式事务组件的三种模式：

- TCC
- SAGA
- 两阶段提交（2PC）

面向新手工程师，按步骤即可跑通。

## 1. 准备工作

先在组件目录编译并安装到本地仓库：

```bash
cd C:\\code\\meets\\dtx-manager
mvn -DskipTests=true install
```

## 2. 运行 Demo

```bash
cd C:\\code\\meets\\dtx-manager\demo-order-payment
mvn -q exec:java
```

运行后你会看到三段输出：TCC / SAGA / 2PC 各执行一遍，并打印订单、支付、库存状态。最后会额外演示一次“失败重试调度器”。

## 3. 项目结构说明

```
C:\\code\\meets\\dtx-manager\demo-order-payment
└─ src\main\java\com\meets\demo
   ├─ common    # 简化的领域对象和服务（订单/库存/支付）
   ├─ tcc       # TCC 参与者实现
   ├─ saga      # SAGA 步骤实现
   ├─ twophase  # 2PC 参与者实现
   └─ DemoApplication.java
```

## 4. 业务设定（简化版）

- **订单服务**：创建订单、确认订单、取消订单
- **库存服务**：预留库存、扣减库存、释放库存
- **支付服务**：冻结资金、扣款、退款

所有数据存放在内存仓库中，便于理解流程。

## 5. TCC 说明

TCC 的流程是 **Try / Confirm / Cancel**：

1. Try：尝试资源预留
2. Confirm：全部成功后确认
3. Cancel：任一失败则回滚

对应代码：

- 订单：`OrderTccParticipant`
- 库存：`StockTccParticipant`
- 支付：`PaymentTccParticipant`

在 `DemoApplication.runTccDemo()` 中构造了参与者列表，然后调用：

```java
TccTransactionManager tccManager = new DistributedTransactionEngine().tcc();
String txId = tccManager.execute(participants, attributes);
```

## 6. SAGA 说明

SAGA 的流程是 **Action / Compensate**：

1. Action：按步骤执行
2. 任一失败后按逆序执行补偿

对应代码：

- `CreateOrderStep`
- `ReserveStockStep`
- `PayStep`

在 `DemoApplication.runSagaDemo()` 中构造步骤列表，然后调用：

```java
SagaTransactionManager sagaManager = new DistributedTransactionEngine().saga();
String txId = sagaManager.execute(steps, attributes);
```

## 7. 两阶段提交（2PC）说明

2PC 的流程是 **Prepare / Commit / Rollback**：

1. Prepare：预提交，所有参与者先锁定/预留资源
2. Commit：全部准备成功后正式提交
3. Rollback：准备阶段有失败则回滚

对应代码：

- `OrderTwoPhaseParticipant`
- `StockTwoPhaseParticipant`
- `PaymentTwoPhaseParticipant`

在 `DemoApplication.runTwoPhaseDemo()` 中执行：

```java
TwoPhaseTransactionManager twoPhaseManager = new DistributedTransactionEngine().twoPhase();
String txId = twoPhaseManager.execute(participants, attributes);
```

## 8. 业务上下文参数说明

三个模式都使用 `attributes` 传递上下文：

```java
attributes.put("orderId", "TCC-ORDER-1");
attributes.put("userId", "U1001");
attributes.put("sku", "SKU-BOOK");
attributes.put("quantity", "2");
attributes.put("amount", "100");
attributes.put("paymentId", payment.getPaymentId());
```

这些参数会在 `TransactionContext` 中透传给各参与者/步骤。

## 9. 日志埋点示例

本 demo 使用 `DemoLogger` 进行埋点（基于 `java.util.logging`），示例代码分布在：

- `com.meets.demo.common.OrderService` / `PaymentService` / `StockService`
- `com.meets.demo.tcc.*`、`com.meets.demo.saga.*`、`com.meets.demo.twophase.*`

你可以在关键业务动作处埋点，例如：

```java
LOGGER.info("TCC Try: 预留库存 sku=%s quantity=%s", sku, quantity);
```

这些日志帮助你在排查时还原事务执行路径。

## 10. 失败重试调度器

示例调度器位于 `com.meets.demo.retry.RetryScheduler`，用于演示 **失败重试 + 固定退避** 的逻辑。

示例代码（`DemoApplication.runRetryDemo()` 中已演示）：  

```java
RetryScheduler scheduler = new RetryScheduler(1);
RetryJob job = scheduler.schedule(
        "payment-pay",
        new RetryHandler() { /* execute + onGiveUp */ },
        3,
        200,
        300
);
job.await(2000);
scheduler.shutdown(1000);
```

> 真实生产中建议与事务库联动，定时扫描失败状态并重放。

## 11. Spring Boot 集成

已内置 Spring Boot 示例，类名与入口如下：

- 启动类：`com.meets.demo.springboot.DemoSpringBootApplication`
- 配置类：`com.meets.demo.springboot.DemoConfiguration`
- 运行器：`TccDemoRunner` / `SagaDemoRunner` / `TwoPhaseDemoRunner`

运行方式（任选其一）：  

```bash
cd C:\\code\\meets\\demo-order-payment
mvn spring-boot:run -Dspring-boot.run.profiles=tcc
```

可用 profile：`tcc` / `saga` / `twophase`  
如需在业务中使用，可直接把参与者/步骤作为 Bean 注入。

## 12. 如何模拟失败场景

想观察回滚或补偿，可以故意让库存不足，例如：

- 将 `stockService.initStock("SKU-BOOK", 10)` 改成 `stockService.initStock("SKU-BOOK", 1)`
- 或把 `quantity` 改为 5

然后重新运行，你将看到异常抛出，并触发 Cancel / Compensate / Rollback。

## 13. 进一步学习建议

- 在组件层实现 `TransactionRepository`，将事务记录写入数据库
- 增加失败重试、补偿重放、定时扫描等机制
- 将参与者实现为真实业务服务

---

如果你希望我继续补充：

- 日志埋点示例
- 失败重试调度器
- 与 Spring Boot 集成

告诉我即可。

