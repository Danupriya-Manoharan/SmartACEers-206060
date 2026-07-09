package com.smartaceers.proofchecker.core;

/**
 * Enumeration representing the severity levels of validation findings.
 * 
 * @author SmartACEers Team
 * @version 1.0.0
 */
public enum Severity {
    /**
     * Critical severity - Must fix, will cause production issues.
     * These findings represent serious problems that could lead to:
     * - Data loss
     * - System failures
     * - Security vulnerabilities
     * - Unhandled errors
     */
    CRITICAL("Critical", 4),
    
    /**
     * High severity - Should fix soon, significant issues.
     * These findings represent important problems that could lead to:
     * - Data inconsistency
     * - Error handling gaps
     * - Security concerns
     * - Reliability issues
     */
    HIGH("High", 3),
    
    /**
     * Medium severity - Should fix, may cause issues.
     * These findings represent potential problems that could lead to:
     * - Performance degradation
     * - Maintainability issues
     * - Best practice violations
     */
    MEDIUM("Medium", 2),
    
    /**
     * Low severity - Consider fixing, minor issues.
     * These findings represent suggestions for improvement:
     * - Code quality improvements
     * - Documentation suggestions
     * - Style consistency
     */
    LOW("Low", 1),
    
    /**
     * Warning severity - Deprecated, use MEDIUM instead.
     * @deprecated Use MEDIUM for consistency
     */
    @Deprecated
    WARNING("Warning", 2),
    
    /**
     * Info severity - Informational, no immediate action required.
     * These findings provide general information.
     */
    INFO("Info", 0);
    
    private final String displayName;
    private final int priority;
    
    /**
     * Constructor for Severity enum.
     * 
     * @param displayName Human-readable name for the severity level
     * @param priority Numeric priority (higher = more severe)
     */
    Severity(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }
    
    /**
     * Gets the display name of the severity level.
     * 
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the numeric priority of the severity level.
     * 
     * @return Priority value (higher = more severe)
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Converts Eclipse marker severity to our Severity enum.
     *
     * @param markerSeverity Eclipse marker severity constant
     * @return Corresponding Severity enum value
     */
    public static Severity fromMarkerSeverity(int markerSeverity) {
        switch (markerSeverity) {
            case 2: // IMarker.SEVERITY_ERROR
                return CRITICAL;
            case 1: // IMarker.SEVERITY_WARNING
                return MEDIUM;
            case 0: // IMarker.SEVERITY_INFO
                return INFO;
            default:
                return MEDIUM;
        }
    }
    
    /**
     * Converts our Severity enum to Eclipse marker severity.
     *
     * @return Eclipse marker severity constant
     */
    public int toMarkerSeverity() {
        switch (this) {
            case CRITICAL:
            case HIGH:
                return 2; // IMarker.SEVERITY_ERROR
            case MEDIUM:
            case WARNING:
                return 1; // IMarker.SEVERITY_WARNING
            case LOW:
            case INFO:
                return 0; // IMarker.SEVERITY_INFO
            default:
                return 1; // IMarker.SEVERITY_WARNING
        }
    }
}

// Made with Bob
