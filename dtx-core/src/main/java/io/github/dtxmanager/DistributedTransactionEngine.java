package io.github.dtxmanager;

import io.github.dtxmanager.core.IdGenerator;
import io.github.dtxmanager.core.InMemoryTransactionRepository;
import io.github.dtxmanager.core.TransactionRepository;
import io.github.dtxmanager.core.UuidIdGenerator;
import io.github.dtxmanager.saga.SagaTransactionManager;
import io.github.dtxmanager.tcc.TccTransactionManager;
import io.github.dtxmanager.twophase.TwoPhaseTransactionManager;

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

