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
    // 事务持久化仓库
    private final TransactionRepository repository;
    // 事务/分支 ID 生成器
    private final IdGenerator idGenerator;

    public DistributedTransactionEngine() {
        // 默认使用内存仓库与 UUID 生成器，适合本地演示
        this(new InMemoryTransactionRepository(), new UuidIdGenerator());
    }

    public DistributedTransactionEngine(TransactionRepository repository, IdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public TccTransactionManager tcc() {
        // 返回 TCC 管理器实例
        return new TccTransactionManager(repository, idGenerator);
    }

    public SagaTransactionManager saga() {
        // 返回 SAGA 管理器实例
        return new SagaTransactionManager(repository, idGenerator);
    }

    public TwoPhaseTransactionManager twoPhase() {
        // 返回 2PC 管理器实例
        return new TwoPhaseTransactionManager(repository, idGenerator);
    }

    public TransactionRepository getRepository() {
        return repository;
    }
}

