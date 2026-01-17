package com.meets.demo.springboot;

import com.meets.demo.common.DemoLogger;
import com.meets.demo.common.PaymentInfo;
import com.meets.demo.common.OrderService;
import com.meets.demo.common.PaymentService;
import com.meets.demo.common.StockService;
import com.meets.demo.saga.CreateOrderStep;
import com.meets.demo.saga.PayStep;
import com.meets.demo.saga.ReserveStockStep;
import com.meets.dtx.DistributedTransactionEngine;
import com.meets.dtx.saga.SagaTransactionManager;
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
