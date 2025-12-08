package com.cagepa.pmg.infra;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static Logger instance;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {
        // Private constructor to prevent instantiation
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void logInfo(String message) {
        System.out.println("[INFO] " + LocalDateTime.now().format(formatter) + " - " + message);
    }

    public void logError(String message) {
        System.err.println("[ERROR] " + LocalDateTime.now().format(formatter) + " - " + message);
    }
}
