package io.github.zaragozamartin91.splitter;

import java.util.logging.Logger;

/**
 * Utility logger that delegates to java.util.logging.Logger
 * only when the system property 'logEnabled' is set to true.
 */
public class ZeLogger {
    private final Logger logger;

    private ZeLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Returns a ZeLogger instance for the given name.
     *
     * @param name The name of the logger.
     * @return A new ZeLogger instance wrapping a java.util.logging.Logger.
     */
    public static ZeLogger getLogger(String name) {
        return new ZeLogger(Logger.getLogger(name));
    }

    /**
     * Logs an info message if logEnabled system property is true.
     *
     * @param msg The message to log.
     */
    public void info(String msg) {
        if (Boolean.getBoolean("logEnabled")) {
            logger.info(msg);
        }
    }

    /**
     * Logs a warning message if logEnabled system property is true.
     *
     * @param msg The message to log.
     */
    public void warning(String msg) {
        if (Boolean.getBoolean("logEnabled")) {
            logger.warning(msg);
        }
    }
}
