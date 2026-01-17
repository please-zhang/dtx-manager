package io.github.dtxmanager.demo.saga;

import io.github.dtxmanager.demo.common.DemoLogger;
import io.github.dtxmanager.demo.common.PaymentService;
import io.github.dtxmanager.core.TransactionContext;
import io.github.dtxmanager.saga.SagaStep;

/**
 * 支付扣款的 SAGA 步骤。
 */
public class PayStep implements SagaStep {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(PayStep.class);
    private final PaymentService paymentService;

    public PayStep(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public String getName() {
        return "pay";
    }

    @Override
    public void action(TransactionContext context) {
        String paymentId = context.getAttributes().get("paymentId");
        LOGGER.info("SAGA Action: 扣款 paymentId=%s", paymentId);
        paymentService.pay(paymentId);
    }

    @Override
    public void compensate(TransactionContext context) {
        String paymentId = context.getAttributes().get("paymentId");
        LOGGER.warn("SAGA Compensate: 退款 paymentId=%s", paymentId);
        paymentService.refund(paymentId);
    }
}

