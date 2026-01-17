package com.meets.dtx.core;

import java.util.UUID;

/**
 * 默认使用 UUID 的 ID 生成器。
 */
public class UuidIdGenerator implements IdGenerator {
    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }
}
