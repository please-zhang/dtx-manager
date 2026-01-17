package io.github.dtxmanager.core;

/**
 * 组件内部统一异常。
 */
public class DtxException extends RuntimeException {
    public DtxException(String message) {
        super(message);
    }

    public DtxException(String message, Throwable cause) {
        super(message, cause);
    }
}

