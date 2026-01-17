package io.github.dtxmanager.demo.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付仓库，模拟数据库。
 */
public class PaymentRepository {
    private final Map<String, PaymentInfo> store = new ConcurrentHashMap<String, PaymentInfo>();

    public PaymentInfo find(String paymentId) {
        return store.get(paymentId);
    }

    public void save(PaymentInfo payment) {
        store.put(payment.getPaymentId(), payment);
    }
}

