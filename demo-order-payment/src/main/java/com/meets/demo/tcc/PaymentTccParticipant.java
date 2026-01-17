package com.meets.demo.tcc;

import com.meets.demo.common.DemoLogger;
import com.meets.demo.common.PaymentService;
import com.meets.dtx.core.TransactionContext;
import com.meets.dtx.tcc.TccParticipant;

/**
 * 支付 TCC 参与者。
 */
public class PaymentTccParticipant implements TccParticipant {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(PaymentTccParticipant.class);
    private final PaymentService paymentService;

    public PaymentTccParticipant(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public String getName() {
        return "payment";
    }

    @Override
    public void tryAction(TransactionContext context) {
        String paymentId = context.getAttributes().get("paymentId");
        // Try 阶段冻结资金
        LOGGER.info("TCC Try: 冻结支付 paymentId=%s", paymentId);
        paymentService.freeze(paymentId);
    }

    @Override
    public void confirm(TransactionContext context) {
        String paymentId = context.getAttributes().get("paymentId");
        // Confirm 阶段真正扣款
        LOGGER.info("TCC Confirm: 扣款 paymentId=%s", paymentId);
        paymentService.pay(paymentId);
    }

    @Override
    public void cancel(TransactionContext context) {
        String paymentId = context.getAttributes().get("paymentId");
        // Cancel 阶段释放资金
        LOGGER.warn("TCC Cancel: 退款 paymentId=%s", paymentId);
        paymentService.refund(paymentId);
    }
}
