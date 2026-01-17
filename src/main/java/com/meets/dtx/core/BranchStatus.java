package com.meets.dtx.core;

/**
 * 分支状态，用于表达各模式步骤的执行结果。
 */
public enum BranchStatus {
    NEW,

    // TCC
    TRY_SUCCESS,
    TRY_FAILED,
    CONFIRM_SUCCESS,
    CONFIRM_FAILED,
    CANCEL_SUCCESS,
    CANCEL_FAILED,

    // SAGA
    ACTION_SUCCESS,
    ACTION_FAILED,
    COMPENSATE_SUCCESS,
    COMPENSATE_FAILED,

    // Two-Phase
    PREPARED,
    PREPARE_FAILED,
    COMMIT_SUCCESS,
    COMMIT_FAILED,
    ROLLBACK_SUCCESS,
    ROLLBACK_FAILED
}
