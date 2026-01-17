package com.meets.dtx.core;

/**
 * ID 生成器，便于替换为雪花等策略。
 */
public interface IdGenerator {
    String nextId();
}
