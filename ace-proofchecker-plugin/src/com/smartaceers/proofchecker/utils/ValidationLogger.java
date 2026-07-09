package com.smartaceers.proofchecker.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for logging validation output to both console and file.
 * Creates log files in the workspace .metadata directory.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public class ValidationLogger {
    
    private static final String LOG_DIR_NAME = ".ace-proofcheck-logs";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    private static PrintWriter currentLogWriter = null;
    private static String currentLogFile = null;
    
    /**
     * Initializes a new log file for the current validation session.
     * 
     * @param flowFileName Name of the message flow being validated
     * @return Path to the created log file, or null if creation failed
     */
    public static String initializeLogFile(String flowFileName) {
        try {
            // Get user home directory
            String userHome = System.getProperty("user.home");
            File logDir = new File(userHome, LOG_DIR_NAME);
            
            // Create log directory if it doesn't exist
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // Create log file with timestamp
            String timestamp = DATE_FORMAT.format(new Date());
            String sanitizedFileName = flowFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
            String logFileName = String.format("validation_%s_%s.log", sanitizedFileName, timestamp);
            File logFile = new File(logDir, logFileName);
            
            // Close previous writer if exists
            closeLogFile();
            
            // Create new writer
            currentLogWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)), true);
            currentLogFile = logFile.getAbsolutePath();
            
            // Write header
            log("================================================================================");
            log("ACE Proofcheck Validation Log");
            log("Flow: " + flowFileName);
            log("Started: " + TIMESTAMP_FORMAT.format(new Date()));
            log("Log File: " + currentLogFile);
            log("================================================================================");
            log("");
            
            return currentLogFile;
            
        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Logs a message to both console and file.
     * 
     * @param message Message to log
     */
    public static void log(String message) {
        // Always write to console
        System.out.println(message);
        
        // Write to file if available
        if (currentLogWriter != null) {
            currentLogWriter.println(message);
            currentLogWriter.flush(); // Ensure immediate write
        }
    }
    
    /**
     * Logs a message with timestamp to both console and file.
     * 
     * @param message Message to log
     */
    public static void logWithTimestamp(String message) {
        String timestampedMessage = "[" + TIMESTAMP_FORMAT.format(new Date()) + "] " + message;
        log(timestampedMessage);
    }
    
    /**
     * Logs a separator line.
     */
    public static void logSeparator() {
        log("--------------------------------------------------------------------------------");
    }
    
    /**
     * Logs a header with separators.
     * 
     * @param header Header text
     */
    public static void logHeader(String header) {
        log("");
        log("================================================================================");
        log(header);
        log("================================================================================");
    }
    
    /**
     * Logs an error message.
     * 
     * @param message Error message
     */
    public static void logError(String message) {
        log("ERROR: " + message);
    }
    
    /**
     * Logs an error with exception details.
     * 
     * @param message Error message
     * @param e Exception
     */
    public static void logError(String message, Exception e) {
        log("ERROR: " + message);
        log("Exception: " + e.getClass().getName() + ": " + e.getMessage());
        if (currentLogWriter != null) {
            e.printStackTrace(currentLogWriter);
        }
        e.printStackTrace(); // Also to console
    }
    
    /**
     * Closes the current log file.
     */
    public static void closeLogFile() {
        if (currentLogWriter != null) {
            log("");
            log("================================================================================");
            log("Validation Log Ended: " + TIMESTAMP_FORMAT.format(new Date()));
            log("================================================================================");
            
            currentLogWriter.close();
            currentLogWriter = null;
        }
    }
    
    /**
     * Gets the path to the current log file.
     * 
     * @return Log file path, or null if no log file is active
     */
    public static String getCurrentLogFile() {
        return currentLogFile;
    }
    
    /**
     * Cleans up old log files (keeps last 10 files).
     */
    public static void cleanupOldLogs() {
        try {
            String userHome = System.getProperty("user.home");
            File logDir = new File(userHome, LOG_DIR_NAME);
            
            if (!logDir.exists()) {
                return;
            }
            
            File[] logFiles = logDir.listFiles((dir, name) -> name.startsWith("validation_") && name.endsWith(".log"));
            
            if (logFiles != null && logFiles.length > 10) {
                // Sort by last modified date
                java.util.Arrays.sort(logFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
                
                // Delete oldest files, keep last 10
                for (int i = 0; i < logFiles.length - 10; i++) {
                    logFiles[i].delete();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to cleanup old logs: " + e.getMessage());
        }
    }
}

// Made with Bob
