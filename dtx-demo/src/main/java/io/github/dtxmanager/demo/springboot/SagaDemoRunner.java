package io.github.dtxmanager.demo.springboot;

import io.github.dtxmanager.demo.common.DemoLogger;
import io.github.dtxmanager.demo.common.PaymentInfo;
import io.github.dtxmanager.demo.common.OrderService;
import io.github.dtxmanager.demo.common.PaymentService;
import io.github.dtxmanager.demo.common.StockService;
import io.github.dtxmanager.demo.saga.CreateOrderStep;
import io.github.dtxmanager.demo.saga.PayStep;
import io.github.dtxmanager.demo.saga.ReserveStockStep;
import io.github.dtxmanager.DistributedTransactionEngine;
import io.github.dtxmanager.saga.SagaTransactionManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot + SAGA 示例。
 */
@Component
@Profile("saga")
public class SagaDemoRunner implements CommandLineRunner {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(SagaDemoRunner.class);

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final StockService stockService;
    private final DistributedTransactionEngine engine;

    public SagaDemoRunner(OrderService orderService,
                          PaymentService paymentService,
                          StockService stockService,
                          DistributedTransactionEngine engine) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.stockService = stockService;
        this.engine = engine;
    }

    @Override
    public void run(String... args) {
        LOGGER.info("[Spring Boot] SAGA 模式开始");
        stockService.initStock("SKU-SB-SAGA", 6);
        PaymentInfo payment = paymentService.initPayment("SB-SAGA-ORDER-1", 999);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("orderId", "SB-SAGA-ORDER-1");
        attributes.put("userId", "U-SB-02");
        attributes.put("sku", "SKU-SB-SAGA");
        attributes.put("quantity", "1");
        attributes.put("amount", "999");
        attributes.put("paymentId", payment.getPaymentId());

        SagaTransactionManager sagaManager = engine.saga();
        String txId = sagaManager.execute(
                Arrays.asList(
                        new CreateOrderStep(orderService),
                        new ReserveStockStep(stockService),
                        new PayStep(paymentService)
                ),
                attributes
        );

        LOGGER.info("[Spring Boot] SAGA 事务完成 txId=%s", txId);
    }
}

