package io.github.dtxmanager.core;

/**
 * 事务分支记录。
 */
public class BranchRecord {
    private final String branchId;
    private final String name;
    private BranchStatus status;
    private String message;
    private final long createdAt;
    private long updatedAt;

    public BranchRecord(String branchId, String name) {
        this.branchId = branchId;
        this.name = name;
        this.status = BranchStatus.NEW;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getName() {
        return name;
    }

    public BranchStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void updateStatus(BranchStatus status, String message) {
        this.status = status;
        this.message = message;
        this.updatedAt = System.currentTimeMillis();
    }
}

