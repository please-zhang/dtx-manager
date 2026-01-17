package com.meets.dtx;

import com.meets.dtx.core.IdGenerator;
import com.meets.dtx.core.InMemoryTransactionRepository;
import com.meets.dtx.core.TransactionRepository;
import com.meets.dtx.core.UuidIdGenerator;
import com.meets.dtx.saga.SagaTransactionManager;
import com.meets.dtx.tcc.TccTransactionManager;
import com.meets.dtx.twophase.TwoPhaseTransactionManager;

/**
 * 对外的统一入口，快速获取各模式的事务管理器。
 */
public class DistributedTransactionEngine {
    private final TransactionRepository repository;
    private final IdGenerator idGenerator;

    public DistributedTransactionEngine() {
        this(new InMemoryTransactionRepository(), new UuidIdGenerator());
    }

    public DistributedTransactionEngine(TransactionRepository repository, IdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public TccTransactionManager tcc() {
        return new TccTransactionManager(repository, idGenerator);
    }

    public SagaTransactionManager saga() {
        return new SagaTransactionManager(repository, idGenerator);
    }

    public TwoPhaseTransactionManager twoPhase() {
        return new TwoPhaseTransactionManager(repository, idGenerator);
    }

    public TransactionRepository getRepository() {
        return repository;
    }
}
