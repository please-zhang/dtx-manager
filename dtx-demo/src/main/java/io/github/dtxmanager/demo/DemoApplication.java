package io.github.dtxmanager.demo;

import io.github.dtxmanager.demo.common.DemoLogger;
import io.github.dtxmanager.demo.common.OrderInfo;
import io.github.dtxmanager.demo.common.OrderRepository;
import io.github.dtxmanager.demo.common.OrderService;
import io.github.dtxmanager.demo.common.PaymentInfo;
import io.github.dtxmanager.demo.common.PaymentRepository;
import io.github.dtxmanager.demo.common.PaymentService;
import io.github.dtxmanager.demo.common.StockInfo;
import io.github.dtxmanager.demo.common.StockRepository;
import io.github.dtxmanager.demo.common.StockService;
import io.github.dtxmanager.demo.saga.CreateOrderStep;
import io.github.dtxmanager.demo.saga.PayStep;
import io.github.dtxmanager.demo.saga.ReserveStockStep;
import io.github.dtxmanager.demo.tcc.OrderTccParticipant;
import io.github.dtxmanager.demo.tcc.PaymentTccParticipant;
import io.github.dtxmanager.demo.tcc.StockTccParticipant;
import io.github.dtxmanager.demo.twophase.OrderTwoPhaseParticipant;
import io.github.dtxmanager.demo.twophase.PaymentTwoPhaseParticipant;
import io.github.dtxmanager.demo.twophase.StockTwoPhaseParticipant;
import io.github.dtxmanager.demo.retry.RetryHandler;
import io.github.dtxmanager.demo.retry.RetryJob;
import io.github.dtxmanager.demo.retry.RetryScheduler;
import io.github.dtxmanager.DistributedTransactionEngine;
import io.github.dtxmanager.saga.SagaTransactionManager;
import io.github.dtxmanager.tcc.TccTransactionManager;
import io.github.dtxmanager.twophase.TwoPhaseTransactionManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 演示入口：模拟下单 + 支付 + 库存的分布式事务。
 */
public class DemoApplication {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        runTccDemo();
        runSagaDemo();
        runTwoPhaseDemo();
        runRetryDemo();
    }

    private static void runTccDemo() {
        LOGGER.info("===== TCC 模式演示 =====");
        OrderRepository orderRepository = new OrderRepository();
        PaymentRepository paymentRepository = new PaymentRepository();
        StockRepository stockRepository = new StockRepository();

        OrderService orderService = new OrderService(orderRepository);
        PaymentService paymentService = new PaymentService(paymentRepository);
        StockService stockService = new StockService(stockRepository);

        stockService.initStock("SKU-BOOK", 10);
        PaymentInfo payment = paymentService.initPayment("TCC-ORDER-1", 100);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("orderId", "TCC-ORDER-1");
        attributes.put("userId", "U1001");
        attributes.put("sku", "SKU-BOOK");
        attributes.put("quantity", "2");
        attributes.put("amount", "100");
        attributes.put("paymentId", payment.getPaymentId());

        TccTransactionManager tccManager = new DistributedTransactionEngine().tcc();
        String txId = tccManager.execute(
                Arrays.asList(
                        new OrderTccParticipant(orderService),
                        new StockTccParticipant(stockService),
                        new PaymentTccParticipant(paymentService)
                ),
                attributes
        );

        LOGGER.info("TCC 事务 ID: %s", txId);
        printState(orderService, paymentService, stockService, "TCC-ORDER-1", payment.getPaymentId(), "SKU-BOOK");
    }

    private static void runSagaDemo() {
        LOGGER.info("===== SAGA 模式演示 =====");
        OrderRepository orderRepository = new OrderRepository();
        PaymentRepository paymentRepository = new PaymentRepository();
        StockRepository stockRepository = new StockRepository();

        OrderService orderService = new OrderService(orderRepository);
        PaymentService paymentService = new PaymentService(paymentRepository);
        StockService stockService = new StockService(stockRepository);

        stockService.initStock("SKU-PHONE", 5);
        PaymentInfo payment = paymentService.initPayment("SAGA-ORDER-1", 3999);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("orderId", "SAGA-ORDER-1");
        attributes.put("userId", "U2001");
        attributes.put("sku", "SKU-PHONE");
        attributes.put("quantity", "1");
        attributes.put("amount", "3999");
        attributes.put("paymentId", payment.getPaymentId());

        List<io.github.dtxmanager.saga.SagaStep> steps = Arrays.asList(
                new CreateOrderStep(orderService),
                new ReserveStockStep(stockService),
                new PayStep(paymentService)
        );

        SagaTransactionManager sagaManager = new DistributedTransactionEngine().saga();
        String txId = sagaManager.execute(steps, attributes);

        LOGGER.info("SAGA 事务 ID: %s", txId);
        printState(orderService, paymentService, stockService, "SAGA-ORDER-1", payment.getPaymentId(), "SKU-PHONE");
    }

    private static void runTwoPhaseDemo() {
        LOGGER.info("===== 2PC 模式演示 =====");
        OrderRepository orderRepository = new OrderRepository();
        PaymentRepository paymentRepository = new PaymentRepository();
        StockRepository stockRepository = new StockRepository();

        OrderService orderService = new OrderService(orderRepository);
        PaymentService paymentService = new PaymentService(paymentRepository);
        StockService stockService = new StockService(stockRepository);

        stockService.initStock("SKU-LAPTOP", 3);
        PaymentInfo payment = paymentService.initPayment("2PC-ORDER-1", 6999);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("orderId", "2PC-ORDER-1");
        attributes.put("userId", "U3001");
        attributes.put("sku", "SKU-LAPTOP");
        attributes.put("quantity", "1");
        attributes.put("amount", "6999");
        attributes.put("paymentId", payment.getPaymentId());

        TwoPhaseTransactionManager twoPhaseManager = new DistributedTransactionEngine().twoPhase();
        String txId = twoPhaseManager.execute(
                Arrays.asList(
                        new OrderTwoPhaseParticipant(orderService),
                        new StockTwoPhaseParticipant(stockService),
                        new PaymentTwoPhaseParticipant(paymentService)
                ),
                attributes
        );

        LOGGER.info("2PC 事务 ID: %s", txId);
        printState(orderService, paymentService, stockService, "2PC-ORDER-1", payment.getPaymentId(), "SKU-LAPTOP");
    }

    private static void runRetryDemo() {
        LOGGER.info("===== 失败重试调度器演示 =====");
        PaymentRepository paymentRepository = new PaymentRepository();
        PaymentService paymentService = new PaymentService(paymentRepository);

        PaymentInfo payment = paymentService.initPayment("RETRY-ORDER-1", 199);
        paymentService.freeze(payment.getPaymentId());

        RetryScheduler scheduler = new RetryScheduler(1);
        AtomicBoolean failedOnce = new AtomicBoolean(false);

        RetryJob job = scheduler.schedule(
                "payment-pay",
                new RetryHandler() {
                    @Override
                    public void execute(int attempt) throws Exception {
                        if (!failedOnce.get()) {
                            failedOnce.set(true);
                            throw new IllegalStateException("模拟网络抖动，第一次失败");
                        }
                        paymentService.pay(payment.getPaymentId());
                    }

                    @Override
                    public void onGiveUp(int attempts, Exception lastError) {
                        LOGGER.error("重试最终失败 attempts=%s error=%s", attempts, lastError.getMessage());
                    }
                },
                3,
                200,
                300
        );

        try {
            job.await(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            scheduler.shutdown(1000);
        }

        LOGGER.info("重试后支付状态: %s", paymentService.find(payment.getPaymentId()).getStatus());
    }

    private static void printState(OrderService orderService,
                                   PaymentService paymentService,
                                   StockService stockService,
                                   String orderId,
                                   String paymentId,
                                   String sku) {
        OrderInfo order = orderService.find(orderId);
        PaymentInfo payment = paymentService.find(paymentId);
        StockInfo stock = stockService.find(sku);

        LOGGER.info("订单状态: %s", order == null ? "null" : order.getStatus());
        LOGGER.info("支付状态: %s", payment == null ? "null" : payment.getStatus());
        if (stock == null) {
            LOGGER.info("库存状态: null");
        } else {
            LOGGER.info("库存状态: %s, available=%s, reserved=%s",
                    stock.getStatus(), stock.getAvailable(), stock.getReserved());
        }
    }
}

