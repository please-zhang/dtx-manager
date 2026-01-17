package com.meets.demo.saga;

import com.meets.demo.common.DemoLogger;
import com.meets.demo.common.StockService;
import com.meets.dtx.core.TransactionContext;
import com.meets.dtx.saga.SagaStep;

/**
 * 预留库存的 SAGA 步骤。
 */
public class ReserveStockStep implements SagaStep {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(ReserveStockStep.class);
    private final StockService stockService;

    public ReserveStockStep(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public String getName() {
        return "reserve-stock";
    }

    @Override
    public void action(TransactionContext context) {
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));
        LOGGER.info("SAGA Action: 预留库存 sku=%s quantity=%s", sku, quantity);
        stockService.reserve(sku, quantity);
    }

    @Override
    public void compensate(TransactionContext context) {
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));
        LOGGER.warn("SAGA Compensate: 释放库存 sku=%s quantity=%s", sku, quantity);
        stockService.release(sku, quantity);
    }
}
