package io.github.dtxmanager.demo.retry;

/**
 * 失败重试的业务回调。
 */
public interface RetryHandler {
    /**
     * 执行一次任务。
     *
     * @param attempt 当前是第几次尝试（从 1 开始）
     */
    void execute(int attempt) throws Exception;

    /**
     * 当超过最大重试次数时回调。
     */
    void onGiveUp(int attempts, Exception lastError);
}

