package com.meets.dtx.saga;

import com.meets.dtx.core.TransactionContext;

/**
 * SAGA 步骤定义。
 */
public interface SagaStep {
    String getName();

    void action(TransactionContext context) throws Exception;

    void compensate(TransactionContext context) throws Exception;
}
