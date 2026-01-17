package com.meets.demo.common;

/**
 * 简化的库存对象。
 */
public class StockInfo {
    private final String sku;
    private int available;
    private int reserved;
    private StockStatus status;

    public StockInfo(String sku, int available) {
        this.sku = sku;
        this.available = available;
        this.status = StockStatus.INIT;
    }

    public String getSku() {
        return sku;
    }

    public int getAvailable() {
        return available;
    }

    public int getReserved() {
        return reserved;
    }

    public StockStatus getStatus() {
        return status;
    }

    public void reserve(int quantity) {
        this.available -= quantity;
        this.reserved += quantity;
        this.status = StockStatus.RESERVED;
    }

    public void deduct(int quantity) {
        this.reserved -= quantity;
        this.status = StockStatus.DEDUCTED;
    }

    public void release(int quantity) {
        this.reserved -= quantity;
        this.available += quantity;
        this.status = StockStatus.RELEASED;
    }
}
