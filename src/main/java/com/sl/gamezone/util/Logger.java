package com.sl.gamezone.util;

public class Logger {
    private static final String INFO = "INFO";
    private static final String WARN = "WARN";
    private static final String ERROR = "ERROR";

    public static void info(String... messages) {
        log(INFO, messages);
    }

    public static void warn(String... messages) {
        log(WARN, messages);
    }

    public static void error(String... messages) {
        log(ERROR, messages);
    }

    private static void log(String type, String... messages) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(type).append(" ");
        for (String message : messages)
            logMessage.append(message).append(" ");
        System.out.println(logMessage);
    }
}
