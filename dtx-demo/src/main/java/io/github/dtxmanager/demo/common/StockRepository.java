package io.github.dtxmanager.demo.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 库存仓库，模拟数据库。
 */
public class StockRepository {
    private final Map<String, StockInfo> store = new ConcurrentHashMap<String, StockInfo>();

    public StockInfo find(String sku) {
        return store.get(sku);
    }

    public void save(StockInfo stock) {
        store.put(stock.getSku(), stock);
    }
}

