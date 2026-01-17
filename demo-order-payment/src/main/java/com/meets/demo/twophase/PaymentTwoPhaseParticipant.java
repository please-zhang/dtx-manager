package com.meets.demo.twophase;

import com.meets.demo.common.DemoLogger;
import com.meets.demo.common.PaymentService;
import com.meets.dtx.core.TransactionContext;
import com.meets.dtx.twophase.TwoPhaseParticipant;

/**
 * 支付 2PC 参与者。
 */
public class PaymentTwoPhaseParticipant implements TwoPhaseParticipant {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(PaymentTwoPhaseParticipant.class);
    private final PaymentService paymentService;

    public PaymentTwoPhaseParticipant(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public String getName() {
        return "payment";
    }

    @Override
    public void prepare(TransactionContext context) {
        String paymentId = context.getAttributes().get("paymentId");
        LOGGER.info("2PC Prepare: 冻结支付 paymentId=%s", paymentId);
        paymentService.freeze(paymentId);
    }

    @Override
    public void commit(TransactionContext context) {
        String paymentId = context.getAttributes().get("paymentId");
        LOGGER.info("2PC Commit: 扣款 paymentId=%s", paymentId);
        paymentService.pay(paymentId);
    }

    @Override
    public void rollback(TransactionContext context) {
        String paymentId = context.getAttributes().get("paymentId");
        LOGGER.warn("2PC Rollback: 退款 paymentId=%s", paymentId);
        paymentService.refund(paymentId);
    }
}
