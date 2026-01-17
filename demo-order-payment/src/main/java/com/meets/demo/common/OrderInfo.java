package com.meets.demo.common;

/**
 * 简化的订单对象。
 */
public class OrderInfo {
    private final String orderId;
    private final String userId;
    private final String sku;
    private final int quantity;
    private OrderStatus status;

    public OrderInfo(String orderId, String userId, String sku, int quantity) {
        this.orderId = orderId;
        this.userId = userId;
        this.sku = sku;
        this.quantity = quantity;
        this.status = OrderStatus.INIT;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
