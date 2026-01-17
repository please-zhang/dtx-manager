package io.github.dtxmanager.tcc;

import io.github.dtxmanager.core.TransactionContext;

/**
 * TCC 模式参与者。
 */
public interface TccParticipant {
    String getName();

    void tryAction(TransactionContext context) throws Exception;

    void confirm(TransactionContext context) throws Exception;

    void cancel(TransactionContext context) throws Exception;
}

