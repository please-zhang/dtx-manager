package com.meets.demo.springboot;

import com.meets.demo.common.DemoLogger;
import com.meets.demo.common.PaymentInfo;
import com.meets.demo.common.OrderService;
import com.meets.demo.common.PaymentService;
import com.meets.demo.common.StockService;
import com.meets.demo.twophase.OrderTwoPhaseParticipant;
import com.meets.demo.twophase.PaymentTwoPhaseParticipant;
import com.meets.demo.twophase.StockTwoPhaseParticipant;
import com.meets.dtx.DistributedTransactionEngine;
import com.meets.dtx.twophase.TwoPhaseTransactionManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot + 2PC 示例。
 */
@Component
@Profile("twophase")
public class TwoPhaseDemoRunner implements CommandLineRunner {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(TwoPhaseDemoRunner.class);

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final StockService stockService;
    private final DistributedTransactionEngine engine;

    public TwoPhaseDemoRunner(OrderService orderService,
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
        LOGGER.info("[Spring Boot] 2PC 模式开始");
        stockService.initStock("SKU-SB-2PC", 4);
        PaymentInfo payment = paymentService.initPayment("SB-2PC-ORDER-1", 1599);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("orderId", "SB-2PC-ORDER-1");
        attributes.put("userId", "U-SB-03");
        attributes.put("sku", "SKU-SB-2PC");
        attributes.put("quantity", "1");
        attributes.put("amount", "1599");
        attributes.put("paymentId", payment.getPaymentId());

        TwoPhaseTransactionManager twoPhaseManager = engine.twoPhase();
        String txId = twoPhaseManager.execute(
                Arrays.asList(
                        new OrderTwoPhaseParticipant(orderService),
                        new StockTwoPhaseParticipant(stockService),
                        new PaymentTwoPhaseParticipant(paymentService)
                ),
                attributes
        );

        LOGGER.info("[Spring Boot] 2PC 事务完成 txId=%s", txId);
    }
}
