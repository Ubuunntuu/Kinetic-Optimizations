package com.kinetic.optimizations.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class KineticLogger {

    private static final Logger LOGGER = LogManager.getLogger("KineticOptimizations");
    private static volatile boolean debugEnabled = false;

    private KineticLogger() {
    }

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void info(String message, Object... args) {
        LOGGER.info("[Kinetic] " + format(message, args));
    }

    public static void warn(String message, Object... args) {
        LOGGER.warn("[Kinetic] " + format(message, args));
    }

    public static void error(String message, Throwable throwable, Object... args) {
        LOGGER.error("[Kinetic] " + format(message, args), throwable);
    }

    public static void error(String message, Object... args) {
        LOGGER.error("[Kinetic] " + format(message, args));
    }

    public static void debug(String message, Object... args) {
        if (debugEnabled) {
            LOGGER.info("[Kinetic-Debug] " + format(message, args));
        }
    }

    private static String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        try {
            return String.format(message, args);
        } catch (Exception e) {
            return message;
        }
    }
}
