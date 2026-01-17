package io.github.dtxmanager.saga;

import io.github.dtxmanager.core.TransactionContext;

/**
 * SAGA 步骤定义。
 */
public interface SagaStep {
    String getName();

    void action(TransactionContext context) throws Exception;

    void compensate(TransactionContext context) throws Exception;
}

