package com.cagepa.pmg.infra;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static Logger instance;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private java.io.PrintWriter logWriter;

    private Logger() {
        try {
            // Append mode
            logWriter = new java.io.PrintWriter(new java.io.FileWriter("system.log", true), true);
        } catch (java.io.IOException e) {
            System.err.println("CRITICAL: Failed to open system.log: " + e.getMessage());
        }
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void logInfo(String message) {
        if (logWriter != null) {
            logWriter.println("[INFO] " + LocalDateTime.now().format(formatter) + " - " + message);
        }
    }

    public void logError(String message) {
        if (logWriter != null) {
            logWriter.println("[ERROR] " + LocalDateTime.now().format(formatter) + " - " + message);
        }
    }
}
