package io.github.dtxmanager.demo.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单仓库，模拟数据库。
 */
public class OrderRepository {
    private final Map<String, OrderInfo> store = new ConcurrentHashMap<String, OrderInfo>();

    public OrderInfo find(String orderId) {
        return store.get(orderId);
    }

    public void save(OrderInfo order) {
        store.put(order.getOrderId(), order);
    }
}

