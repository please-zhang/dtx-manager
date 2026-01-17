package io.github.dtxmanager.demo.twophase;

import io.github.dtxmanager.demo.common.DemoLogger;
import io.github.dtxmanager.demo.common.StockService;
import io.github.dtxmanager.core.TransactionContext;
import io.github.dtxmanager.twophase.TwoPhaseParticipant;

/**
 * 库存 2PC 参与者。
 */
public class StockTwoPhaseParticipant implements TwoPhaseParticipant {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(StockTwoPhaseParticipant.class);
    private final StockService stockService;

    public StockTwoPhaseParticipant(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public String getName() {
        return "stock";
    }

    @Override
    public void prepare(TransactionContext context) {
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));
        LOGGER.info("2PC Prepare: 预留库存 sku=%s quantity=%s", sku, quantity);
        stockService.reserve(sku, quantity);
    }

    @Override
    public void commit(TransactionContext context) {
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));
        LOGGER.info("2PC Commit: 扣减库存 sku=%s quantity=%s", sku, quantity);
        stockService.deduct(sku, quantity);
    }

    @Override
    public void rollback(TransactionContext context) {
        String sku = context.getAttributes().get("sku");
        int quantity = Integer.parseInt(context.getAttributes().get("quantity"));
        LOGGER.warn("2PC Rollback: 释放库存 sku=%s quantity=%s", sku, quantity);
        stockService.release(sku, quantity);
    }
}

