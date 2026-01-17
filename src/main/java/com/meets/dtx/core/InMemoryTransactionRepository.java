package com.meets.dtx.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版本的事务仓库，适合单机或测试场景。
 */
public class InMemoryTransactionRepository implements TransactionRepository {
    private final Map<String, TransactionRecord> store = new ConcurrentHashMap<String, TransactionRecord>();

    @Override
    public void save(TransactionRecord record) {
        store.put(record.getTransactionId(), record);
    }

    @Override
    public void update(TransactionRecord record) {
        store.put(record.getTransactionId(), record);
    }

    @Override
    public TransactionRecord find(String transactionId) {
        return store.get(transactionId);
    }

    @Override
    public List<TransactionRecord> listByStatus(TransactionStatus status) {
        List<TransactionRecord> result = new ArrayList<TransactionRecord>();
        for (TransactionRecord record : store.values()) {
            if (record.getStatus() == status) {
                result.add(record);
            }
        }
        return result;
    }
}
