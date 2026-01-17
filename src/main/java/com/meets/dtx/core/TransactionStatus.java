package com.meets.dtx.core;

/**
 * 事务状态枚举。
 */
public enum TransactionStatus {
    NEW,
    TRYING,
    CONFIRMING,
    CANCELLING,
    PREPARING,
    COMMITTING,
    ROLLING_BACK,
    COMPLETED,
    FAILED
}
