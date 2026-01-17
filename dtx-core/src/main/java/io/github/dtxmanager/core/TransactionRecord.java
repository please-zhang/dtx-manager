package io.github.dtxmanager.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事务记录，持久化层保存的核心对象。
 */
public class TransactionRecord {
    // 全局事务 ID
    private final String transactionId;
    // 事务模式（TCC/SAGA/2PC）
    private final TransactionPattern pattern;
    // 事务当前状态
    private TransactionStatus status;
    // 创建时间戳
    private final long createdAt;
    // 最近更新时间戳
    private long updatedAt;
    // 业务透传属性
    private final Map<String, String> attributes;
    // 事务分支列表
    private final List<BranchRecord> branches;

    public TransactionRecord(String transactionId, TransactionPattern pattern, Map<String, String> attributes) {
        this.transactionId = transactionId;
        this.pattern = pattern;
        this.status = TransactionStatus.NEW;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.attributes = new HashMap<String, String>();
        // 如果调用方提供了属性，则复制到内部 Map 中
        if (attributes != null) {
            // 使用拷贝避免外部引用变更影响事务记录
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
        // 更新事务状态并刷新更新时间
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public void addBranch(BranchRecord branch) {
        // 追加分支记录并刷新更新时间
        this.branches.add(branch);
        this.updatedAt = System.currentTimeMillis();
    }
}

