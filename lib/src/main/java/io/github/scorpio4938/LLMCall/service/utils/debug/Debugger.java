package com.scorpio4938.LLMCall.service.utils.debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for debugging messages.
 * 
 * @since 1.0.0
 */
public class Debugger {
    private static final boolean ENABLED = true; // Set to false to disable debugging logs

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final boolean ENABLED_TIME = true; // Set to false to disable debugging time

    public static boolean getEnable() {
        return ENABLED;
    }

    /**
     * Logs a debug message with a timestamp.
     *
     * @param message The debug message to log.
     */
    public static void log(String message) {
        log("DEBUG", message);
    }

    public static void log(Object message) {
        log("DEBUG", message);
    }

    /**
     * Logs a debug message with a timestamp and a log type.
     *
     * @param logType The type of log (e.g., "info", "warning", "error").
     * @param message The debug message to log.
     */
    public static void log(String logType, String message) {
        if (ENABLED) {
            System.out.println(formatTimestamp() + " [" + logType.toUpperCase() + "] " + message);
        }
    }

    public static void log(String logType, Object message) {
        if (ENABLED) {
            System.out.println(formatTimestamp() + " [" + logType.toUpperCase() + "] " + message);
        }
    }

    /**
     * Logs an object's string representation.
     *
     * @param object The object to log.
     * @apiNote Need some overridden .toString function of the object
     */
    public static void inspect(Object object) {
        if (ENABLED) {
            String description = (object == null) ? "null" : object.toString();
            System.out.println(formatTimestamp() + " [INSPECT] " + description);
        }
    }

    /**
     * Logs an object's string representation.
     *
     * @param label  The label for the object.
     * @param object The object to log.
     * @apiNote Need some overridden .toString function of the object
     */
    public static void inspect(String label, Object object) {
        if (ENABLED) {
            String description = (object == null) ? "null" : object.toString();
            System.out.println(formatTimestamp() + " [INSPECT] " + label + ": " + description);
        }
    }

    /**
     * Logs the stack trace of an exception.
     *
     * @param exception The exception to log.
     */
    public static void logException(Exception exception) {
        if (ENABLED) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            System.out.println(formatTimestamp() + " [EXCEPTION] " + sw);
        }
    }

    public static void logTime() {
        if (ENABLED_TIME) {
            System.out.println(" [TIME] " + formatTimestamp());
        }
    }

    /**
     * Formats the current timestamp for logging.
     *
     * @return A formatted timestamp.
     */
    private static String formatTimestamp() {
        if (ENABLED_TIME) {
            return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        } else {
            return "";
        }
    }
}
