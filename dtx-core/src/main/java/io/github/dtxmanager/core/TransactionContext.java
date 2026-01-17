package io.github.dtxmanager.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行上下文，传递给具体业务参与者。
 */
public class TransactionContext {
    // 全局事务 ID
    private final String transactionId;
    // 事务模式
    private final TransactionPattern pattern;
    // 分支 ID
    private final String branchId;
    // 分支名称（参与者名称）
    private final String branchName;
    // 业务透传属性
    private final Map<String, String> attributes;

    public TransactionContext(String transactionId, TransactionPattern pattern, String branchId, String branchName,
                              Map<String, String> attributes) {
        this.transactionId = transactionId;
        this.pattern = pattern;
        this.branchId = branchId;
        this.branchName = branchName;
        this.attributes = new HashMap<String, String>();
        // 调用方未传入属性时，保持空 Map，避免空指针
        if (attributes != null) {
            // 拷贝一份，避免外部修改影响上下文内容
            this.attributes.putAll(attributes);
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionPattern getPattern() {
        return pattern;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public Map<String, String> getAttributes() {
        // 只读视图，避免外部修改
        return Collections.unmodifiableMap(attributes);
    }
}

