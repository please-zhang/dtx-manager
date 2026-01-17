package io.github.dtxmanager.demo.saga;

import io.github.dtxmanager.demo.common.DemoLogger;
import io.github.dtxmanager.demo.common.OrderService;
import io.github.dtxmanager.core.TransactionContext;
import io.github.dtxmanager.saga.SagaStep;

/**
 * 创建订单的 SAGA 步骤。
 */
public class CreateOrderStep implements SagaStep {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(CreateOrderStep.class);
    private final OrderService orderService;

    public CreateOrderStep(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String getName() {
        return "create-order";
    }

    @Override
    public void action(TransactionContext context) {
        String orderId = context.getAttributes().get("orderId");
        String userId = context.getAttributes().get("userId");
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));

        LOGGER.info("SAGA Action: 创建订单 orderId=%s", orderId);
        orderService.createOrder(orderId, userId, sku, quantity);
        orderService.confirm(orderId);
    }

    @Override
    public void compensate(TransactionContext context) {
        String orderId = context.getAttributes().get("orderId");
        LOGGER.warn("SAGA Compensate: 取消订单 orderId=%s", orderId);
        orderService.cancel(orderId);
    }
}

