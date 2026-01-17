package com.meets.demo.tcc;

import com.meets.demo.common.DemoLogger;
import com.meets.demo.common.StockService;
import com.meets.dtx.core.TransactionContext;
import com.meets.dtx.tcc.TccParticipant;

/**
 * 库存 TCC 参与者。
 */
public class StockTccParticipant implements TccParticipant {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(StockTccParticipant.class);
    private final StockService stockService;

    public StockTccParticipant(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public String getName() {
        return "stock";
    }

    @Override
    public void tryAction(TransactionContext context) {
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));
        // Try 阶段预留库存
        LOGGER.info("TCC Try: 预留库存 sku=%s quantity=%s", sku, quantity);
        stockService.reserve(sku, quantity);
    }

    @Override
    public void confirm(TransactionContext context) {
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));
        // Confirm 阶段扣减库存
        LOGGER.info("TCC Confirm: 扣减库存 sku=%s quantity=%s", sku, quantity);
        stockService.deduct(sku, quantity);
    }

    @Override
    public void cancel(TransactionContext context) {
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));
        // Cancel 阶段释放库存
        LOGGER.warn("TCC Cancel: 释放库存 sku=%s quantity=%s", sku, quantity);
        stockService.release(sku, quantity);
    }
}
