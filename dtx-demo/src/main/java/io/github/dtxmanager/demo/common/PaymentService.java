package io.github.dtxmanager.demo.common;

import java.util.UUID;

/**
 * 支付服务，模拟冻结、扣款、退款。
 */
public class PaymentService {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(PaymentService.class);
    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public PaymentInfo initPayment(String orderId, int amount) {
        String paymentId = UUID.randomUUID().toString();
        PaymentInfo payment = new PaymentInfo(paymentId, orderId, amount);
        repository.save(payment);
        LOGGER.info("初始化支付单 paymentId=%s orderId=%s amount=%s", paymentId, orderId, amount);
        return payment;
    }

    public void freeze(String paymentId) {
        PaymentInfo payment = repository.find(paymentId);
        if (payment != null && payment.getStatus() == PaymentStatus.INIT) {
            payment.setStatus(PaymentStatus.FROZEN);
            LOGGER.info("冻结资金 paymentId=%s", paymentId);
        }
    }

    public void pay(String paymentId) {
        PaymentInfo payment = repository.find(paymentId);
        if (payment != null && payment.getStatus() != PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.PAID);
            LOGGER.info("完成扣款 paymentId=%s", paymentId);
        }
    }

    public void refund(String paymentId) {
        PaymentInfo payment = repository.find(paymentId);
        if (payment != null && payment.getStatus() != PaymentStatus.REFUNDED) {
            payment.setStatus(PaymentStatus.REFUNDED);
            LOGGER.info("执行退款 paymentId=%s", paymentId);
        }
    }

    public PaymentInfo find(String paymentId) {
        return repository.find(paymentId);
    }
}

