package io.github.dtxmanager.demo.common;

/**
 * 库存服务，模拟预留、扣减、释放。
 */
public class StockService {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(StockService.class);
    private final StockRepository repository;

    public StockService(StockRepository repository) {
        this.repository = repository;
    }

    public void initStock(String sku, int available) {
        repository.save(new StockInfo(sku, available));
        LOGGER.info("初始化库存 sku=%s available=%s", sku, available);
    }

    public void reserve(String sku, int quantity) {
        StockInfo stock = repository.find(sku);
        if (stock == null) {
            throw new IllegalStateException("库存不存在: " + sku);
        }
        if (stock.getAvailable() < quantity) {
            throw new IllegalStateException("库存不足: " + sku);
        }
        stock.reserve(quantity);
        LOGGER.info("预留库存 sku=%s quantity=%s", sku, quantity);
    }

    public void deduct(String sku, int quantity) {
        StockInfo stock = repository.find(sku);
        if (stock == null) {
            throw new IllegalStateException("库存不存在: " + sku);
        }
        if (stock.getReserved() < quantity) {
            throw new IllegalStateException("预留库存不足: " + sku);
        }
        stock.deduct(quantity);
        LOGGER.info("扣减库存 sku=%s quantity=%s", sku, quantity);
    }

    public void release(String sku, int quantity) {
        StockInfo stock = repository.find(sku);
        if (stock == null) {
            return;
        }
        if (stock.getReserved() >= quantity) {
            stock.release(quantity);
            LOGGER.info("释放库存 sku=%s quantity=%s", sku, quantity);
        }
    }

    public StockInfo find(String sku) {
        return repository.find(sku);
    }
}

