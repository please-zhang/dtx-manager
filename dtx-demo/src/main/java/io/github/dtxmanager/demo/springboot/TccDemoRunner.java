package io.github.dtxmanager.demo.springboot;

import io.github.dtxmanager.demo.common.DemoLogger;
import io.github.dtxmanager.demo.common.PaymentInfo;
import io.github.dtxmanager.demo.common.OrderService;
import io.github.dtxmanager.demo.common.PaymentService;
import io.github.dtxmanager.demo.common.StockService;
import io.github.dtxmanager.demo.tcc.OrderTccParticipant;
import io.github.dtxmanager.demo.tcc.PaymentTccParticipant;
import io.github.dtxmanager.demo.tcc.StockTccParticipant;
import io.github.dtxmanager.DistributedTransactionEngine;
import io.github.dtxmanager.tcc.TccTransactionManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot + TCC 示例。
 */
@Component
@Profile("tcc")
public class TccDemoRunner implements CommandLineRunner {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(TccDemoRunner.class);

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final StockService stockService;
    private final DistributedTransactionEngine engine;

    public TccDemoRunner(OrderService orderService,
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
        LOGGER.info("[Spring Boot] TCC 模式开始");
        stockService.initStock("SKU-SB-TCC", 8);
        PaymentInfo payment = paymentService.initPayment("SB-TCC-ORDER-1", 520);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("orderId", "SB-TCC-ORDER-1");
        attributes.put("userId", "U-SB-01");
        attributes.put("sku", "SKU-SB-TCC");
        attributes.put("quantity", "2");
        attributes.put("amount", "520");
        attributes.put("paymentId", payment.getPaymentId());

        TccTransactionManager tccManager = engine.tcc();
        String txId = tccManager.execute(
                Arrays.asList(
                        new OrderTccParticipant(orderService),
                        new StockTccParticipant(stockService),
                        new PaymentTccParticipant(paymentService)
                ),
                attributes
        );

        LOGGER.info("[Spring Boot] TCC 事务完成 txId=%s", txId);
    }
}

