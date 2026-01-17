package io.github.dtxmanager.demo.common;

/**
 * 简化的支付对象。
 */
public class PaymentInfo {
    private final String paymentId;
    private final String orderId;
    private final int amount;
    private PaymentStatus status;

    public PaymentInfo(String paymentId, String orderId, int amount) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.INIT;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}

