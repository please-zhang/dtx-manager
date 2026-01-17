package io.github.dtxmanager.demo.tcc;

import io.github.dtxmanager.demo.common.DemoLogger;
import io.github.dtxmanager.demo.common.OrderService;
import io.github.dtxmanager.core.TransactionContext;
import io.github.dtxmanager.tcc.TccParticipant;

/**
 * 订单 TCC 参与者。
 */
public class OrderTccParticipant implements TccParticipant {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(OrderTccParticipant.class);
    private final OrderService orderService;

    public OrderTccParticipant(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String getName() {
        return "order";
    }

    @Override
    public void tryAction(TransactionContext context) {
        String orderId = context.getAttributes().get("orderId");
        String userId = context.getAttributes().get("userId");
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));

        // Try 阶段创建订单并标记为 TRY_SUCCESS
        LOGGER.info("TCC Try: 创建订单 orderId=%s", orderId);
        orderService.createOrder(orderId, userId, sku, quantity);
        orderService.markTrySuccess(orderId);
    }

    @Override
    public void confirm(TransactionContext context) {
        String orderId = context.getAttributes().get("orderId");
        // Confirm 阶段确认订单
        LOGGER.info("TCC Confirm: 确认订单 orderId=%s", orderId);
        orderService.confirm(orderId);
    }

    @Override
    public void cancel(TransactionContext context) {
        String orderId = context.getAttributes().get("orderId");
        // Cancel 阶段取消订单
        LOGGER.warn("TCC Cancel: 取消订单 orderId=%s", orderId);
        orderService.cancel(orderId);
    }
}

