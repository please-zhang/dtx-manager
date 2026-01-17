package com.meets.dtx.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事务记录，持久化层保存的核心对象。
 */
public class TransactionRecord {
    private final String transactionId;
    private final TransactionPattern pattern;
    private TransactionStatus status;
    private final long createdAt;
    private long updatedAt;
    private final Map<String, String> attributes;
    private final List<BranchRecord> branches;

    public TransactionRecord(String transactionId, TransactionPattern pattern, Map<String, String> attributes) {
        this.transactionId = transactionId;
        this.pattern = pattern;
        this.status = TransactionStatus.NEW;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.attributes = new HashMap<String, String>();
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
        this.branches = new ArrayList<BranchRecord>();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionPattern getPattern() {
        return pattern;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public List<BranchRecord> getBranches() {
        return Collections.unmodifiableList(branches);
    }

    public void updateStatus(TransactionStatus status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public void addBranch(BranchRecord branch) {
        this.branches.add(branch);
        this.updatedAt = System.currentTimeMillis();
    }
}
