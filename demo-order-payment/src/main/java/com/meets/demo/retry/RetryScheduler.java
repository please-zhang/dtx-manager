package com.meets.demo.retry;

import com.meets.demo.common.DemoLogger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的失败重试调度器（演示用）。
 */
public class RetryScheduler {
    private static final DemoLogger LOGGER = DemoLogger.getLogger(RetryScheduler.class);

    private final ScheduledExecutorService executorService;

    public RetryScheduler(int threads) {
        this.executorService = new ScheduledThreadPoolExecutor(threads);
    }

    /**
     * 提交一个带重试的任务。
     *
     * @param taskName      任务名称
     * @param handler       任务回调
     * @param maxAttempts   最大尝试次数
     * @param initialDelay  首次延迟（毫秒）
     * @param backoffMillis 每次失败后的固定退避（毫秒）
     */
    public RetryJob schedule(String taskName,
                             RetryHandler handler,
                             int maxAttempts,
                             long initialDelay,
                             long backoffMillis) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts 必须大于 0");
        }

        RetryJob job = new RetryJob();
        AtomicInteger attempts = new AtomicInteger(0);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                int attempt = attempts.incrementAndGet();
                try {
                    LOGGER.info("重试任务开始 task=%s attempt=%s", taskName, attempt);
                    handler.execute(attempt);
                    LOGGER.info("重试任务成功 task=%s attempt=%s", taskName, attempt);
                    job.markDone();
                } catch (Exception ex) {
                    LOGGER.warn("重试任务失败 task=%s attempt=%s error=%s", taskName, attempt, ex.getMessage());
                    if (attempt >= maxAttempts) {
                        handler.onGiveUp(attempt, ex);
                        job.markDone();
                        return;
                    }
                    executorService.schedule(this, backoffMillis, TimeUnit.MILLISECONDS);
                }
            }
        };

        executorService.schedule(task, initialDelay, TimeUnit.MILLISECONDS);
        return job;
    }

    public void shutdown(long timeoutMillis) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}
