package com.meets.dtx.twophase;

import com.meets.dtx.core.TransactionContext;

/**
 * 二阶段提交参与者。
 */
public interface TwoPhaseParticipant {
    String getName();

    void prepare(TransactionContext context) throws Exception;

    void commit(TransactionContext context) throws Exception;

    void rollback(TransactionContext context) throws Exception;
}
