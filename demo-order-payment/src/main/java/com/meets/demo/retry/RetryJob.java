package com.meets.demo.retry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 重试任务的句柄，便于等待完成。
 */
public class RetryJob {
    private final CountDownLatch done = new CountDownLatch(1);

    void markDone() {
        done.countDown();
    }

    public boolean await(long timeoutMillis) throws InterruptedException {
        return done.await(timeoutMillis, TimeUnit.MILLISECONDS);
    }
}
