package io.github.dtxmanager.demo.common;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 简单的日志封装，便于演示埋点。
 */
public final class DemoLogger {
    private final Logger logger;

    private DemoLogger(Logger logger) {
        this.logger = logger;
    }

    public static DemoLogger getLogger(Class<?> type) {
        return new DemoLogger(Logger.getLogger(type.getName()));
    }

    public void info(String message, Object... args) {
        logger.log(Level.INFO, format(message, args));
    }

    public void warn(String message, Object... args) {
        logger.log(Level.WARNING, format(message, args));
    }

    public void error(String message, Object... args) {
        logger.log(Level.SEVERE, format(message, args));
    }

    private String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return String.format(Locale.ROOT, message, args);
    }
}

