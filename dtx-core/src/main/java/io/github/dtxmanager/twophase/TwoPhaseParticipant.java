package io.github.dtxmanager.twophase;

import io.github.dtxmanager.core.TransactionContext;

/**
 * 二阶段提交参与者。
 */
public interface TwoPhaseParticipant {
    String getName();

    void prepare(TransactionContext context) throws Exception;

    void commit(TransactionContext context) throws Exception;

    void rollback(TransactionContext context) throws Exception;
}

