package io.github.dtxmanager.core;

import java.util.List;

/**
 * 事务持久化接口，可对接数据库、消息等存储。
 */
public interface TransactionRepository {
    void save(TransactionRecord record);

    void update(TransactionRecord record);

    TransactionRecord find(String transactionId);

    List<TransactionRecord> listByStatus(TransactionStatus status);
}

