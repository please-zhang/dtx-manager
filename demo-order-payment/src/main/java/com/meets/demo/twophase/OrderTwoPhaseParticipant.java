package com.meets.demo.twophase;

import com.meets.demo.common.DemoLogger;
import com.meets.demo.common.OrderService;
import com.meets.dtx.core.TransactionContext;
import com.meets.dtx.twophase.TwoPhaseParticipant;

/**
 * 订单 2PC 参与者。
 */
public class OrderTwoPhaseParticipant implements TwoPhaseParticipant {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(OrderTwoPhaseParticipant.class);
    private final OrderService orderService;

    public OrderTwoPhaseParticipant(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String getName() {
        return "order";
    }

    @Override
    public void prepare(TransactionContext context) {
        String orderId = context.getAttributes().get("orderId");
        String userId = context.getAttributes().get("userId");
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));

        // Prepare 阶段创建订单并标记 TRY_SUCCESS（相当于预提交）
        LOGGER.info("2PC Prepare: 创建订单 orderId=%s", orderId);
        orderService.createOrder(orderId, userId, sku, quantity);
        orderService.markTrySuccess(orderId);
    }

    @Override
    public void commit(TransactionContext context) {
        String orderId = context.getAttributes().get("orderId");
        LOGGER.info("2PC Commit: 确认订单 orderId=%s", orderId);
        orderService.confirm(orderId);
    }

    @Override
    public void rollback(TransactionContext context) {
        String orderId = context.getAttributes().get("orderId");
        LOGGER.warn("2PC Rollback: 取消订单 orderId=%s", orderId);
        orderService.cancel(orderId);
    }
}
