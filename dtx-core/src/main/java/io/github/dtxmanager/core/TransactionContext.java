package io.github.dtxmanager.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行上下文，传递给具体业务参与者。
 */
public class TransactionContext {
    private final String transactionId;
    private final TransactionPattern pattern;
    private final String branchId;
    private final String branchName;
    private final Map<String, String> attributes;

    public TransactionContext(String transactionId, TransactionPattern pattern, String branchId, String branchName,
                              Map<String, String> attributes) {
        this.transactionId = transactionId;
        this.pattern = pattern;
        this.branchId = branchId;
        this.branchName = branchName;
        this.attributes = new HashMap<String, String>();
        if (attributes != null) {
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
        return Collections.unmodifiableMap(attributes);
    }
}

