package io.github.dtxmanager.core;

/**
 * 事务分支记录。
 */
public class BranchRecord {
    // 分支唯一标识
    private final String branchId;
    // 业务参与者名称
    private final String name;
    // 当前分支状态
    private BranchStatus status;
    // 失败或说明信息
    private String message;
    // 创建时间戳
    private final long createdAt;
    // 最近更新时间戳
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
        // 更新状态时同步保存说明与更新时间
        this.status = status;
        this.message = message;
        this.updatedAt = System.currentTimeMillis();
    }
}

